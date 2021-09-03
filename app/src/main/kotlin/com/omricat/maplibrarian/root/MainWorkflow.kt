package com.omricat.maplibrarian.root

import com.omricat.maplibrarian.auth.AuthResult
import com.omricat.maplibrarian.auth.AuthService
import com.omricat.maplibrarian.auth.AuthWorkflow
import com.omricat.maplibrarian.auth.User
import com.omricat.maplibrarian.root.MainWorkflow.State
import com.omricat.maplibrarian.userdetails.UserDetailsWorkFlow
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action

class MainWorkflow(
    private val authService: AuthService,
    private val authWorkflow: AuthWorkflow,
    private val userDetailsWorkFlow: UserDetailsWorkFlow
) : StatefulWorkflow<Unit, State, Nothing, MainScreen>() {

    sealed class State {
        object Authorizing : State()
        data class UserDetails(val user: User) : State()
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State = State.Authorizing

    override fun render(props: Unit, state: State, context: RenderContext): MainScreen =
        when (state) {
            is State.Authorizing -> context.renderChild(
                authWorkflow,
                Unit
            ) { authResult: AuthResult ->
                action {
                    if (authResult is AuthResult.Authenticated) {
                        this.state = State.UserDetails(authResult.user)
                    } else {
                        this.state = State.Authorizing
                    }
                }
            }
            is State.UserDetails ->
                context.renderChild(userDetailsWorkFlow, state.user) {
                    authService.signOut()
                    action {
                        this.state = State.Authorizing
                    }
                }
        }

    override fun snapshotState(state: State): Snapshot? = null // TODO: Implement snapshots
}

typealias MainScreen = Any
