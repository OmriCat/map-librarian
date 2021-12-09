package com.omricat.maplibrarian.chartlist

import com.omricat.maplibrarian.chartlist.ChartDetailsWorkflow.Props
import com.omricat.maplibrarian.chartlist.ChartDetailsWorkflow.State
import com.omricat.maplibrarian.chartlist.ChartDetailsWorkflow.State.EditingChart
import com.omricat.maplibrarian.chartlist.ChartDetailsWorkflow.State.ShowingDetails
import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.maplibrarian.model.User
import com.omricat.workflow.eventHandler
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

public class ChartDetailsWorkflow(private val editChartWorkflow: AddNewChartWorkflow) :
    StatefulWorkflow<Props, State, Nothing, ChartDetailsScreen>() {

    public data class Props(val user: User, val chart: DbChartModel)

    @Serializable
    public sealed class State {

        public abstract val chart: DbChartModel

        @Serializable
        public data class ShowingDetails(override val chart: DbChartModel) : State()

        public data class EditingChart(override val chart: DbChartModel) : State()

        internal companion object {
            fun fromSnapshot(snapshot: Snapshot): State =
                Json.decodeFromString(serializer(), snapshot.bytes.utf8())
        }
    }

    override fun initialState(props: Props, snapshot: Snapshot?): State =
        snapshot?.let(State::fromSnapshot) ?: TODO()

    override fun render(
        renderProps: Props,
        renderState: State,
        context: RenderContext
    ): ChartDetailsScreen {
        val showingChartDetailsScreen = ShowingChartDetailsScreen(
            title = renderState.chart.title,
            onEditPressed = context.eventHandler(onEdit(renderState))
        )
        return when (renderState) {
            is ShowingDetails -> {
                showingChartDetailsScreen
            }

            is EditingChart ->
                EditingOverlayDetailsScreen(
                    detailsScreen = showingChartDetailsScreen,
                    overlaidEditingScreen = context.renderChild(
                        editChartWorkflow,
                        props = AddNewChartWorkflow.Props(renderProps.user, renderState.chart)
                    ) { action { } }
                )
        }
    }

    override fun snapshotState(state: State): Snapshot = state.toSnapshot()

    private fun onEdit(renderState: State) = action {
        state = EditingChart(renderState.chart)
    }
}

internal fun State.toSnapshot(): Snapshot =
    Snapshot.of(Json.encodeToString(State.serializer(), this))

public sealed interface ChartDetailsScreen : ChartsScreen

public data class ShowingChartDetailsScreen(
    val title: String,
    val onEditPressed: () -> Unit,
) : ChartDetailsScreen

public data class EditingOverlayDetailsScreen(
    val detailsScreen: ShowingChartDetailsScreen,
    val overlaidEditingScreen: EditingItemScreen
) : ChartDetailsScreen
