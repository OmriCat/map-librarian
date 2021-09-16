package com.omricat.maplibrarian.root

import com.omricat.maplibrarian.User
import com.omricat.maplibrarian.auth.AuthResult
import com.omricat.maplibrarian.auth.AuthResult.Authenticated
import com.omricat.maplibrarian.auth.AuthService
import com.omricat.maplibrarian.auth.AuthWorkflow
import com.omricat.maplibrarian.maplist.MapListWorkflow
import com.omricat.maplibrarian.maplist.MapListWorkflow.Output.LogOut
import com.omricat.maplibrarian.maplist.MapListWorkflow.Props
import com.omricat.maplibrarian.root.MainWorkflow.State
import com.omricat.maplibrarian.root.MainWorkflow.State.MapList
import com.omricat.maplibrarian.root.MainWorkflow.State.Unauthorized
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.renderChild

class MainWorkflow(
    private val authService: AuthService,
    private val authWorkflow: AuthWorkflow,
    private val mapListWorkflow: MapListWorkflow
) : StatefulWorkflow<Unit, State, Nothing, MainScreen>() {

    sealed class State {
        object Unauthorized : State()
        data class MapList(val user: User) : State()
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State = Unauthorized

    override fun render(renderProps: Unit, renderState: State, context: RenderContext): MainScreen =
        when (renderState) {
            is Unauthorized -> context.renderChild(authWorkflow) { authResult: AuthResult ->
                action {
                    state =
                        if (authResult is Authenticated) MapList(authResult.user) else Unauthorized
                }
            }
            is MapList -> context.renderChild(mapListWorkflow, Props(renderState.user)) { output ->
                when (output) {
                    is LogOut -> {
                        authService.signOut()
                        action { state = Unauthorized }
                    }
                }
            }
        }

    override fun snapshotState(state: State): Snapshot? = null // TODO: Implement snapshots
}

typealias MainScreen = Any
