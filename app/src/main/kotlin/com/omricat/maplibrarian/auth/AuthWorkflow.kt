@file:OptIn(WorkflowUiExperimentalApi::class)

package com.omricat.maplibrarian.auth

import com.firebase.ui.auth.data.model.User
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.omricat.maplibrarian.auth.ActualAuthWorkflow.State.LoginPrompt
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.backstack.BackStackScreen

sealed class AuthResult {
    object Unauthenticated : AuthResult()
    data class Authenticated(val user: User) : AuthResult()
}

interface AuthWorkflow : Workflow<Unit, AuthResult, AuthScreen>

interface Credential

internal class ActualAuthWorkflow(private val authService: AuthService) : AuthWorkflow,
    StatefulWorkflow<Unit, ActualAuthWorkflow.State, AuthResult, AuthScreen>() {
    internal sealed class State {
        data class LoginPrompt(val errorMessage: String = "") : State()
        data class AttemptingAuthorization(val credential: Credential) : State()
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State = LoginPrompt()

    override fun render(props: Unit, state: State, context: RenderContext): AuthScreen =
        when (state) {
            is LoginPrompt -> BackStackScreen(
                AuthSubScreen.Login(
                    onLoginClicked = context.eventHandler { credential ->
                        this.state = State.AttemptingAuthorization(credential)
                    },
                    onCancel = context.eventHandler {
                        setOutput(AuthResult.Unauthenticated)
                    }
                )
            )
            is State.AttemptingAuthorization -> {
                context.runningWorker(Worker.from { authService.attemptAuthentication(state.credential) }) { result ->
                    when (result) {
                        is Ok<User> -> action { this.setOutput(AuthResult.Authenticated(result.value)) }
                        is Err -> action {
                            this.state = LoginPrompt(errorMessage = result.error.message)
                        }
                    }
                }
                BackStackScreen(
                    AuthSubScreen.Login(onLoginClicked = { }, onCancel = { }),
                    AuthSubScreen.AttemptingLogin("LoggingIn")
                )
            }
        }

    // Don't need to store state of in progress sign in
    override fun snapshotState(state: State): Snapshot? = null
}

typealias AuthScreen = BackStackScreen<AuthSubScreen>

sealed class AuthSubScreen {
    data class Login(
        val errorMessage: String = "",
        val onLoginClicked: (credential: Credential) -> Unit,
        val onCancel: () -> Unit
    ) : AuthSubScreen()

    data class AttemptingLogin(val message: String) : AuthSubScreen()
}
