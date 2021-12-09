package com.omricat.maplibrarian.chartlist

import com.omricat.maplibrarian.chartlist.ChartDetailsWorkflow.State
import com.omricat.maplibrarian.chartlist.ChartDetailsWorkflow.State.EditingChart
import com.omricat.maplibrarian.chartlist.ChartDetailsWorkflow.State.ShowingDetails
import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.workflow.eventHandler
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

public class ChartDetailsWorkflow(private val editChartWorkflow: AddNewChartWorkflow) :
    StatefulWorkflow<DbChartModel, State, Nothing, ChartDetailsScreen>() {

    @Serializable
    public sealed class State {

        @Serializable
        public data class ShowingDetails(val chart: DbChartModel) : State()

        public data class EditingChart(val chart: DbChartModel) : State()

        internal companion object {
            fun fromSnapshot(snapshot: Snapshot): State =
                Json.decodeFromString(serializer(), snapshot.bytes.utf8())
        }
    }

    override fun initialState(
        props: DbChartModel,
        snapshot: Snapshot?
    ): State = snapshot?.let(State::fromSnapshot) ?: TODO()

    override fun render(
        renderProps: DbChartModel,
        renderState: State,
        context: RenderContext
    ): ChartDetailsScreen = when (renderState) {
        is ShowingDetails -> ChartDetailsScreen(
            title = renderState.chart.title,
            onEditPressed = context.eventHandler(onEdit(renderState))
        )

        is EditingChart ->
            TODO()
    }

    private fun onEdit(renderState: ShowingDetails) = action {
        state = EditingChart(renderState.chart)
    }


    override fun snapshotState(state: State): Snapshot = state.toSnapshot()
}

internal fun State.toSnapshot(): Snapshot =
    Snapshot.of(Json.encodeToString(State.serializer(), this))

public data class ChartDetailsScreen(
    val title: String,
    val onEditPressed: () -> Unit,
) : ChartsScreen
