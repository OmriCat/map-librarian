package com.omricat.maplibrarian.root

import com.omricat.maplibrarian.auth.AuthResult
import com.omricat.maplibrarian.auth.AuthService
import com.omricat.maplibrarian.auth.AuthWorkflow
import com.omricat.maplibrarian.auth.User
import com.omricat.maplibrarian.maplist.MapListWorkflow
import com.omricat.maplibrarian.root.MainWorkflow.State
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action

class MainWorkflow(
    private val authService: AuthService,
    private val authWorkflow: AuthWorkflow,
    private val mapListWorkflow: MapListWorkflow
) : StatefulWorkflow<Unit, State, Nothing, MainScreen>() {

    sealed class State {
        object Authorizing : State()
        data class MapList(val user: User) : State()
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State = State.Authorizing

    override fun render(renderProps: Unit, renderState: State, context: RenderContext): MainScreen =
        when (renderState) {
            is State.Authorizing -> context.renderChild(
                authWorkflow,
                Unit
            ) { authResult: AuthResult ->
                action {
                    if (authResult is AuthResult.Authenticated) {
                        this.state = State.MapList(authResult.user)
                    } else {
                        this.state = State.Authorizing
                    }
                }
            }
            is State.MapList ->
                context.renderChild(mapListWorkflow, MapListWorkflow.Props(renderState.user)) { output: Unit ->
                    authService.signOut()
                    action {
                        this.state = State.Authorizing
                    }
                }
        }

    override fun snapshotState(state: State): Snapshot? = null // TODO: Implement snapshots
}

typealias MainScreen = Any
