package com.omricat.maplibrarian.root

import com.omricat.maplibrarian.auth.AuthResult
import com.omricat.maplibrarian.auth.AuthResult.Authenticated
import com.omricat.maplibrarian.auth.AuthService
import com.omricat.maplibrarian.auth.AuthWorkflow
import com.omricat.maplibrarian.maplist.MapsWorkflow
import com.omricat.maplibrarian.maplist.MapsWorkflow.Output.LogOut
import com.omricat.maplibrarian.maplist.MapsWorkflow.Props
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.root.MainWorkflow.State
import com.omricat.maplibrarian.root.MainWorkflow.State.MapList
import com.omricat.maplibrarian.root.MainWorkflow.State.Unauthorized
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.renderChild

public class MainWorkflow(
    private val authService: AuthService,
    private val authWorkflow: AuthWorkflow,
    private val mapsWorkflow: MapsWorkflow
) : StatefulWorkflow<Unit, State, Nothing, MainScreen>() {

    public sealed class State {
        public object Unauthorized : State()
        public data class MapList(val user: User) : State()
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
            is MapList -> context.renderChild(mapsWorkflow, Props(renderState.user)) { output ->
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

public typealias MainScreen = Any
