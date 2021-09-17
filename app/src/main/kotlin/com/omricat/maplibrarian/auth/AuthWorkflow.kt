@file:OptIn(WorkflowUiExperimentalApi::class)

package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.omricat.maplibrarian.User
import com.omricat.maplibrarian.auth.ActualAuthWorkflow.State.AttemptingAuthorization
import com.omricat.maplibrarian.auth.ActualAuthWorkflow.State.LoginPrompt
import com.omricat.maplibrarian.auth.ActualAuthWorkflow.State.PossibleLoggedInUser
import com.omricat.maplibrarian.auth.AuthResult.Authenticated
import com.omricat.workflow.resultWorker
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.WorkflowAction
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import com.squareup.workflow1.ui.BackPressHandler
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

sealed class AuthResult {
    object Unauthenticated : AuthResult()
    data class Authenticated(val user: User) : AuthResult()
}

sealed interface AuthWorkflow : Workflow<Unit, AuthResult, AuthScreen>

internal class ActualAuthWorkflow(private val authService: AuthService) : AuthWorkflow,
    StatefulWorkflow<Unit, ActualAuthWorkflow.State, AuthResult, AuthScreen>() {
    internal sealed class State {
        object PossibleLoggedInUser : State()
        data class LoginPrompt(val errorMessage: String = "") : State()
        data class AttemptingAuthorization(val credential: Credential) : State()
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State = PossibleLoggedInUser

    override fun render(renderProps: Unit, renderState: State, context: RenderContext): AuthScreen =
        when (renderState) {
            is PossibleLoggedInUser -> {
                context.runningWorker(resolveLoggedInStatus(authService)) { handleAuthResult(it) }
                AuthScreen.AttemptingLogin("")
            }

            is LoginPrompt ->
                AuthScreen.Login(
                    onLoginClicked = context.eventHandler { credential ->
                        this.state = AttemptingAuthorization(credential)
                    },
                    errorMessage = renderState.errorMessage
                )

            is AttemptingAuthorization -> {
                context.runningWorker(
                    attemptAuthentication(authService, renderState.credential)
                ) { handleAuthResult(it) }
                AuthScreen.AttemptingLogin(
                    "LoggingIn",
                    backPressHandler = context.eventHandler { setOutput(AuthResult.Unauthenticated) })
            }
        }

    // Don't need to store state of in progress sign in

    override fun snapshotState(state: State): Snapshot? = null

    private fun attemptAuthentication(
        authService: AuthService,
        credential: Credential
    ): Worker<Result<User, AuthError>> =
        resultWorker(::AuthError) { authService.attemptAuthentication(credential) }

    private fun resolveLoggedInStatus(authService: AuthService): Worker<Result<User, AuthError>> =
        resultWorker(::AuthError) { authService.getSignedInUserIfAny() }

    internal fun handleAuthResult(result: Result<User, AuthError>): WorkflowAction<Unit, State, AuthResult> =
        when (result) {
            is Ok<User> -> onAuthenticated(result.value)
            is Err -> onAuthError(result.error)
        }

    internal fun onAuthError(result: AuthError) =
        action {
            state = LoginPrompt(errorMessage = result.message)
        }

    internal fun onAuthenticated(user: User) = action { setOutput(Authenticated(user)) }
}

sealed class AuthScreen {
    data class Login(
        val errorMessage: String,
        val onLoginClicked: (credential: Credential) -> Unit
    ) : AuthScreen()

    data class AttemptingLogin(
        val message: String,
        val backPressHandler: BackPressHandler? = null
    ) : AuthScreen()
}
