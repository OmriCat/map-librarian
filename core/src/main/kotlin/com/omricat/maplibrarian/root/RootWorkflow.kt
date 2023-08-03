package com.omricat.maplibrarian.root

import com.omricat.logging.Loggable
import com.omricat.logging.Logger
import com.omricat.logging.log
import com.omricat.maplibrarian.auth.AuthResult
import com.omricat.maplibrarian.auth.AuthResult.Authenticated
import com.omricat.maplibrarian.auth.AuthResult.NotAuthenticated
import com.omricat.maplibrarian.auth.AuthWorkflow
import com.omricat.maplibrarian.auth.AuthorizedScreen
import com.omricat.maplibrarian.auth.UserRepository
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
    private val userRepository: UserRepository,
    private val authWorkflow: AuthWorkflow,
    private val chartsWorkflow: ChartsWorkflow,
    override val logger: Logger
) : StatefulWorkflow<Unit, State, Nothing, Screen>(), Loggable {

    public sealed class State {
        public object Unauthorized : State()
        public data class ChartList(val user: User) : State()
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State = Unauthorized

    override fun render(renderProps: Unit, renderState: State, context: RenderContext): Screen =
        when (renderState) {
            is Unauthorized -> {
                context.renderChild(authWorkflow) { authResult: AuthResult ->
                    log { "authResult: $authResult" }
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
        userRepository.signOut()
        state = Unauthorized
    }

    // No need to return anything here. If the user is logged in, then running the auth workflow
    // will retrieve the logged in User and then return them to the MapList, which should restore
    // it's own state.
    override fun snapshotState(state: State): Snapshot? = null
}

public typealias Screen = Any
