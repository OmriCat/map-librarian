package com.omricat.maplibrarian.chartlist

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.omricat.maplibrarian.chartlist.ChartAddItemWorkflow.EditingChartModel
import com.omricat.maplibrarian.chartlist.ChartAddItemWorkflow.Event
import com.omricat.maplibrarian.chartlist.ChartAddItemWorkflow.Event.Discard
import com.omricat.maplibrarian.chartlist.ChartAddItemWorkflow.Event.Saved
import com.omricat.maplibrarian.chartlist.ChartAddItemWorkflow.State
import com.omricat.maplibrarian.chartlist.ChartAddItemWorkflow.State.Editing
import com.omricat.maplibrarian.chartlist.ChartAddItemWorkflow.State.Saving
import com.omricat.maplibrarian.model.ChartModel
import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.model.UserUid
import com.omricat.workflow.eventHandler
import com.omricat.workflow.resultWorker
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker

public class ChartAddItemWorkflow(private val chartsService: ChartsService) :
    StatefulWorkflow<User, State, Event, AddingItemScreen>() {

    public data class EditingChartModel(
        override val title: String,
        override val userId: UserUid
    ) : ChartModel

    public sealed interface State {
        public data class Editing(val chart: EditingChartModel, val errorMessage: String = "") :
            State

        public data class Saving(val chart: ChartModel) : State
    }

    public sealed interface Event {
        public object Discard : Event
        public object Saved : Event
    }

    override fun initialState(props: User, snapshot: Snapshot?): State =
        Editing(EditingChartModel("", props.id))

    override fun render(
        renderProps: User,
        renderState: State,
        context: RenderContext
    ): AddingItemScreen = when (renderState) {
        is Editing -> {
            AddItemScreen(
                chart = renderState.chart,
                errorMessage = renderState.errorMessage,
                onTitleChanged = context.eventHandler(onTitleChanged(renderState)),
                discardChanges = context.eventHandler(::onDiscard),
                saveChanges = context.eventHandler(onSave(renderState.chart))
            )
        }
        is Saving -> {
            context.runningWorker(saveNewItem(renderProps, renderState.chart)) { result ->
                result.map { savedChart -> onNewItemSaved() }
                    .getOrElse { e ->
                        action {
                            state = Editing(
                                chart = editingChart(renderState.chart),
                                errorMessage = e.message
                            )
                        }
                    }
            }
            SavingItemScreen(renderState.chart)
        }
    }

    internal fun onNewItemSaved() = action { setOutput(Saved) }

    override fun snapshotState(state: State): Snapshot? = null // TODO(#18) Implement snapshots

    private fun saveNewItem(
        user: User,
        chart: ChartModel
    ): Worker<Result<DbChartModel, ChartsServiceError>> =
        resultWorker(ChartsServiceError::fromThrowable) { chartsService.addNewChart(user, chart) }

    private fun onSave(chart: ChartModel) = action { state = Saving(chart) }

    private fun onDiscard() = action { setOutput(Discard) }

    private fun onTitleChanged(editingState: Editing) = { newTitle: CharSequence ->
        action { state = Editing(chart = editingState.chart.withTitle(newTitle.toString())) }
    }
}

public sealed interface AddingItemScreen : ChartsScreen {
    public val chart: ChartModel
}

public data class AddItemScreen(
    override val chart: ChartModel,
    val errorMessage: String = "",
    val onTitleChanged: (CharSequence) -> Unit,
    val discardChanges: () -> Unit,
    val saveChanges: () -> Unit
) : AddingItemScreen

public data class SavingItemScreen(override val chart: ChartModel) : AddingItemScreen

private fun EditingChartModel.withTitle(newTitle: String) = copy(title = newTitle)

private fun editingChart(chart: ChartModel) =
    EditingChartModel(title = chart.title.toString(), userId = chart.userId)
