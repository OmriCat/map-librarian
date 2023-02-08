package com.omricat.maplibrarian.root

import com.omricat.maplibrarian.auth.AuthResult
import com.omricat.maplibrarian.auth.AuthResult.Authenticated
import com.omricat.maplibrarian.auth.AuthResult.NotAuthenticated
import com.omricat.maplibrarian.auth.AuthService
import com.omricat.maplibrarian.auth.AuthWorkflow
import com.omricat.maplibrarian.auth.AuthorizedScreen
import com.omricat.maplibrarian.chartlist.ActualChartsWorkflow.Props
import com.omricat.maplibrarian.chartlist.ChartsWorkflow
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.root.RootWorkflow.State
import com.omricat.maplibrarian.root.RootWorkflow.State.ChartList
import com.omricat.maplibrarian.root.RootWorkflow.State.Unauthorized
import com.omricat.workflow.eventHandler
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.renderChild

public class RootWorkflow(
    private val authService: AuthService,
    private val authWorkflow: AuthWorkflow,
    private val chartsWorkflow: ChartsWorkflow
) : StatefulWorkflow<Unit, State, Nothing, Screen>() {

    public sealed class State {
        public object Unauthorized : State()
        public data class ChartList(val user: User) : State()
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State = Unauthorized

    override fun render(renderProps: Unit, renderState: State, context: RenderContext): Screen =
        when (renderState) {
            is Unauthorized -> {
                context.renderChild(authWorkflow) { authResult: AuthResult ->
                    when (authResult) {
                        is Authenticated -> onAuthenticated(authResult)
                        is NotAuthenticated -> unauthorized()
                    }
                }
            }
            is ChartList -> {
                val mapsScreen = context.renderChild(chartsWorkflow, Props(renderState.user))
                AuthorizedScreen(mapsScreen, onLogoutClicked = context.eventHandler(::unauthorized))
            }
        }

    private fun onAuthenticated(authResult: Authenticated) = action {
        state = ChartList(authResult.user)
    }

    private fun unauthorized() = action {
        authService.signOut()
        state = Unauthorized
    }

    // No need to return anything here. If the user is logged in, then running the auth workflow
    // will retrieve the logged in User and then return them to the MapList, which should restore
    // it's own state.
    override fun snapshotState(state: State): Snapshot? = null
}

public typealias Screen = Any
