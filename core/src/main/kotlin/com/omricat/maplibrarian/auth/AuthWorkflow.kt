package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.omricat.maplibrarian.auth.ActualAuthWorkflow.State.AttemptingAuthorization
import com.omricat.maplibrarian.auth.ActualAuthWorkflow.State.LoginPrompt
import com.omricat.maplibrarian.auth.ActualAuthWorkflow.State.PossibleLoggedInUser
import com.omricat.maplibrarian.auth.ActualAuthWorkflow.State.SigningUp
import com.omricat.maplibrarian.auth.AuthResult.Authenticated
import com.omricat.maplibrarian.model.User
import com.omricat.workflow.eventHandler
import com.omricat.workflow.resultWorker
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker

public typealias BackPressHandler = () -> Unit

public sealed class AuthResult {
    public object NotAuthenticated : AuthResult()
    public data class Authenticated(val user: User) : AuthResult()
}

public sealed interface AuthWorkflow : Workflow<Unit, AuthResult, AuthorizingScreen>

public class ActualAuthWorkflow(
    private val userRepository: UserRepository,
    private val signUpWorkflow: SignUpWorkflow
) :
    AuthWorkflow,
    StatefulWorkflow<Unit, ActualAuthWorkflow.State, AuthResult, AuthorizingScreen>() {
    public sealed interface State {
        public object PossibleLoggedInUser : State
        public data class LoginPrompt(val errorMessage: String = "") : State
        public data class AttemptingAuthorization(val credential: Credential) : State
        public object SigningUp : State
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State = PossibleLoggedInUser

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): AuthorizingScreen =
        when (renderState) {
            is PossibleLoggedInUser -> {
                context.runningWorker(
                    resolveLoggedInStatusWorker,
                    handler = ::handlePossibleUserResult
                )
                AuthorizingScreen.AttemptingLogin("")
            }
            is LoginPrompt ->
                AuthorizingScreen.Login(
                    errorMessage = renderState.errorMessage,
                    onLoginClicked = context.eventHandler(::onLoginClicked),
                    onSignUpClicked = context.eventHandler(::onSignUpClicked)
                )
            is AttemptingAuthorization -> {
                context.runningWorker(
                    attemptAuthenticationWorker(renderState.credential),
                    handler = ::handleAuthenticationResult
                )
                AuthorizingScreen.AttemptingLogin(
                    "LoggingIn",
                    backPressHandler =
                        context.eventHandler { setOutput(AuthResult.NotAuthenticated) }
                )
            }
            is SigningUp ->
                context.renderChild(signUpWorkflow, Unit) { output: SignUpOutput ->
                    when (output) {
                        is SignUpOutput.SignUpCancelled -> onNoAuthenticatedUser
                        is SignUpOutput.UserCreated -> onAuthenticated(output.user)
                    }
                }
        }

    // Don't need to store state of in progress sign in
    override fun snapshotState(state: State): Snapshot? = null

    internal fun onAuthError(error: UserRepository.Error) = action {
        state = LoginPrompt(errorMessage = error.message)
    }

    internal fun onAuthenticated(user: User) = action { setOutput(Authenticated(user)) }

    internal val onNoAuthenticatedUser = action { state = LoginPrompt() }

    internal fun onLoginClicked(credential: Credential) = action {
        state = AttemptingAuthorization(credential)
    }

    internal val onSignUpClicked = action { state = SigningUp }

    internal val resolveLoggedInStatusWorker: Worker<Result<User?, UserRepository.Error>>
        get() = resultWorker(::ExceptionWrapperError) { userRepository.getSignedInUserIfAny() }

    internal fun handlePossibleUserResult(result: Result<User?, UserRepository.Error>) =
        result
            .map { maybeUser ->
                maybeUser?.let { user -> onAuthenticated(user) } ?: onNoAuthenticatedUser
            }
            .getOrElse { error -> onAuthError(error) }

    internal fun attemptAuthenticationWorker(
        credential: Credential
    ): Worker<Result<User, UserRepository.Error>> =
        resultWorker(::ExceptionWrapperError) { userRepository.attemptAuthentication(credential) }

    internal fun handleAuthenticationResult(result: Result<User, UserRepository.Error>) =
        result.map { user -> onAuthenticated(user) }.getOrElse { error -> onAuthError(error) }
}

public sealed interface AuthorizingScreen {
    public data class Login(
        val errorMessage: String,
        val onLoginClicked: (credential: Credential) -> Unit,
        val onSignUpClicked: () -> Unit
    ) : AuthorizingScreen

    public data class AttemptingLogin(
        val message: String,
        val backPressHandler: BackPressHandler? = null
    ) : AuthorizingScreen
}
