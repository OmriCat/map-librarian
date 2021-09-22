package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.omricat.maplibrarian.auth.ActualAuthWorkflow.State.AttemptingAuthorization
import com.omricat.maplibrarian.auth.ActualAuthWorkflow.State.LoginPrompt
import com.omricat.maplibrarian.auth.ActualAuthWorkflow.State.PossibleLoggedInUser
import com.omricat.maplibrarian.auth.AuthResult.Authenticated
import com.omricat.maplibrarian.model.User
import com.omricat.workflow.resultWorker
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker

public typealias BackPressHandler = () -> Unit

public sealed class AuthResult {
    public object Unauthenticated : AuthResult()
    public data class Authenticated(val user: User) : AuthResult()
}

public sealed interface AuthWorkflow : Workflow<Unit, AuthResult, AuthorizingScreen>

public class ActualAuthWorkflow(private val authService: AuthService) : AuthWorkflow,
    StatefulWorkflow<Unit, ActualAuthWorkflow.State, AuthResult, AuthorizingScreen>() {
    public sealed class State {
        public object PossibleLoggedInUser : State()
        public data class LoginPrompt(val errorMessage: String = "") : State()
        public data class AttemptingAuthorization(val credential: Credential) : State()
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State = PossibleLoggedInUser

    override fun render(renderProps: Unit, renderState: State, context: RenderContext): AuthorizingScreen =
        when (renderState) {
            is PossibleLoggedInUser -> {
                context.runningWorker(resolveLoggedInStatus(authService)) { result ->
                    result.map { maybeUser ->
                        maybeUser?.let { user -> onAuthenticated(user) }
                            ?: onNoAuthenticatedUser()
                    }.getOrElse { error -> onAuthError(error) }
                }
                AuthorizingScreen.AttemptingLogin("")
            }

            is LoginPrompt ->
                AuthorizingScreen.Login(
                    onLoginClicked = context.eventHandler { credential ->
                        this.state = AttemptingAuthorization(credential)
                    },
                    errorMessage = renderState.errorMessage
                )

            is AttemptingAuthorization -> {
                context.runningWorker(
                    attemptAuthentication(authService, renderState.credential)
                ) { result ->
                    result.map { user -> onAuthenticated(user) }
                        .getOrElse { error -> onAuthError(error) }
                }
                AuthorizingScreen.AttemptingLogin(
                    "LoggingIn",
                    backPressHandler = context.eventHandler { setOutput(AuthResult.Unauthenticated) }
                )
            }
        }

    // Don't need to store state of in progress sign in
    override fun snapshotState(state: State): Snapshot? = null

    internal fun onAuthError(error: AuthError) = action {
        state = LoginPrompt(errorMessage = error.message)
    }

    internal fun onAuthenticated(user: User) = action { setOutput(Authenticated(user)) }

    internal fun onNoAuthenticatedUser() = action { this.state = LoginPrompt() }

    internal companion object {
        internal fun resolveLoggedInStatus(authService: AuthService): Worker<Result<User?, AuthError>> =
            resultWorker(::AuthError) { authService.getSignedInUserIfAny() }

        internal fun attemptAuthentication(
            authService: AuthService,
            credential: Credential
        ): Worker<Result<User, AuthError>> =
            resultWorker(::AuthError) { authService.attemptAuthentication(credential) }
    }
}

public sealed class AuthorizingScreen {
    public data class Login(
        val errorMessage: String,
        val onLoginClicked: (credential: Credential) -> Unit
    ) : AuthorizingScreen()

    public data class AttemptingLogin(
        val message: String,
        val backPressHandler: BackPressHandler? = null
    ) : AuthorizingScreen()
}
