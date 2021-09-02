@file:OptIn(WorkflowUiExperimentalApi::class)

package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.omricat.maplibrarian.auth.ActualAuthWorkflow.State.AttemptingAuthorization
import com.omricat.maplibrarian.auth.ActualAuthWorkflow.State.LoginPrompt
import com.omricat.maplibrarian.auth.ActualAuthWorkflow.State.PossibleLoggedInUser
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.WorkflowAction
import com.squareup.workflow1.action
import com.squareup.workflow1.asWorker
import com.squareup.workflow1.runningWorker
import com.squareup.workflow1.ui.BackPressHandler
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

sealed class AuthResult {
    object Unauthenticated : AuthResult()
    data class Authenticated(val user: User) : AuthResult()
}

interface AuthWorkflow : Workflow<Unit, AuthResult, AuthScreen>

internal class ActualAuthWorkflow(private val authService: AuthService) : AuthWorkflow,
    StatefulWorkflow<Unit, ActualAuthWorkflow.State, AuthResult, AuthScreen>() {
    internal sealed class State {
        object PossibleLoggedInUser : State()
        data class LoginPrompt(val errorMessage: String = "") : State()
        data class AttemptingAuthorization(val credential: Credential) : State()
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State = PossibleLoggedInUser

    override fun render(props: Unit, state: State, context: RenderContext): AuthScreen =
        when (state) {
            is PossibleLoggedInUser -> {
                context.runningWorker(resolveLoggedInStatus(authService)) {
                    when (it) {
                        is Ok<User> -> action { setOutput(AuthResult.Authenticated(it.value)) }
                        is Err<AuthError> -> action { this.state = LoginPrompt() }
                    }
                }
                AuthScreen.ResolvingLoggedInStatus("")
            }

            is LoginPrompt ->
                AuthScreen.Login(
                    onLoginClicked = context.eventHandler { credential ->
                        this.state = AttemptingAuthorization(credential)
                    },
                    errorMessage = state.errorMessage
                )

            is AttemptingAuthorization -> {
                context.runningWorker(
                    attemptAuthentication(authService, state.credential)
                ) { handleAuthResult(it) }
                AuthScreen.AttemptingLogin(
                    "LoggingIn",
                    backPressHandler = context.eventHandler { setOutput(AuthResult.Unauthenticated) })
            }
        }

    private fun resolveLoggedInStatus(authService: AuthService): Worker<Result<User, AuthError>> =
        authService.getSignedInUserIfAny().asWorker()

    // Don't need to store state of in progress sign in
    override fun snapshotState(state: State): Snapshot? = null

    private fun attemptAuthentication(
        authService: AuthService,
        credential: Credential
    ): Worker<Result<User, AuthError>> = authService.attemptAuthentication(credential).asWorker()

    private fun handleAuthResult(result: Result<User, AuthError>): WorkflowAction<Unit, State, AuthResult> =
        when (result) {
            is Ok<User> -> action { setOutput(AuthResult.Authenticated(result.value)) }
            is Err -> action {
                state = LoginPrompt(errorMessage = result.error.message)
            }
        }
}

sealed class AuthScreen {
    data class Login(
        val errorMessage: String,
        val onLoginClicked: (credential: Credential) -> Unit
    ) : AuthScreen()

    data class AttemptingLogin(
        val message: String,
        val backPressHandler: BackPressHandler = {}
    ) : AuthScreen()

    data class ResolvingLoggedInStatus(val message: String) : AuthScreen()
}
