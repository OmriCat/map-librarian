package com.omricat.maplibrarian.chartlist

import com.omricat.maplibrarian.chartlist.ChartDetailsWorkflowImpl.State
import com.omricat.maplibrarian.chartlist.ChartDetailsWorkflowImpl.State.EditingChart
import com.omricat.maplibrarian.chartlist.ChartDetailsWorkflowImpl.State.ShowingDetails
import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.maplibrarian.utils.Snapshotter
import com.omricat.workflow.eventHandler
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.action
import kotlinx.serialization.Serializable
import kotlinx.serialization.StringFormat

public interface ChartDetailsWorkflow : Workflow<DbChartModel, Nothing, ChartDetailsScreen> {
    public companion object {
        public fun instance(
            editChartWorkflow: AddNewChartWorkflow,
            stringFormat: StringFormat
        ): ChartDetailsWorkflow = ChartDetailsWorkflowImpl(editChartWorkflow, stringFormat)
    }
}

internal class ChartDetailsWorkflowImpl(
    private val editChartWorkflow: AddNewChartWorkflow,
    stringFormat: StringFormat
) : StatefulWorkflow<DbChartModel, State, Nothing, ChartDetailsScreen>(), ChartDetailsWorkflow {

    private val snapshotter = State.snapshotter(stringFormat)

    @Serializable
    sealed class State {

        @Serializable data class ShowingDetails(val chart: DbChartModel) : State()

        @Serializable data class EditingChart(val chart: DbChartModel) : State()

        companion object {
            internal fun snapshotter(stringFormat: StringFormat) =
                object : Snapshotter<State>(stringFormat, serializer()) {}
        }
    }

    override fun initialState(props: DbChartModel, snapshot: Snapshot?): State =
        snapshot?.let(snapshotter::valueFromSnapshot) ?: ShowingDetails(props)

    override fun render(
        renderProps: DbChartModel,
        renderState: State,
        context: RenderContext
    ): ChartDetailsScreen =
        when (renderState) {
            is ShowingDetails ->
                ChartDetailsScreen(
                    title = renderState.chart.title,
                    onEditPressed = context.eventHandler(onEdit(renderState))
                )
            is EditingChart -> TODO()
        }

    private fun onEdit(renderState: ShowingDetails) = action {
        state = EditingChart(renderState.chart)
    }

    override fun snapshotState(state: State): Snapshot = snapshotter.snapshotOf(state)
}

public data class ChartDetailsScreen(
    val title: String,
    val onEditPressed: () -> Unit,
) : ChartsScreen
