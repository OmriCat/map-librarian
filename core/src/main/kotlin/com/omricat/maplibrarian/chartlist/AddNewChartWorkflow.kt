package com.omricat.maplibrarian.chartlist

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.omricat.maplibrarian.chartlist.AddNewChartWorkflow.Event
import com.omricat.maplibrarian.chartlist.AddNewChartWorkflow.Event.Discard
import com.omricat.maplibrarian.chartlist.AddNewChartWorkflow.Event.Saved
import com.omricat.maplibrarian.chartlist.AddNewChartWorkflow.State
import com.omricat.maplibrarian.chartlist.AddNewChartWorkflow.State.Editing
import com.omricat.maplibrarian.chartlist.AddNewChartWorkflow.State.Saving
import com.omricat.maplibrarian.model.ChartModel
import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.maplibrarian.model.UnsavedChartModel
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.model.UserUid
import com.omricat.workflow.eventHandler
import com.omricat.workflow.resultWorker
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker

private fun unsavedChartModel(userUid: UserUid, title: String): UnsavedChartModel =
    UnsavedChartModel(userUid, title)

public class AddNewChartWorkflow(private val chartsService: ChartsService) :
    StatefulWorkflow<User, State, Event, AddingItemScreen>() {

    public sealed class State {
        public data class Editing(
            val chart: UnsavedChartModel,
            val errorMessage: String = ""
        ) : State()

        public data class Saving(val chart: UnsavedChartModel) : State()
    }

    public sealed interface Event {
        public object Discard : Event
        public object Saved : Event
    }

    override fun initialState(props: User, snapshot: Snapshot?): State =
        Editing(unsavedChartModel(props.id, ""))

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
        chart: UnsavedChartModel
    ): Worker<Result<DbChartModel, ChartsServiceError>> =
        resultWorker(ChartsServiceError::fromThrowable) { chartsService.addNewChart(user, chart) }

    private fun onSave(chart: UnsavedChartModel) = action { state = Saving(chart) }

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

private fun UnsavedChartModel.withTitle(newTitle: String) = copy(title = newTitle)

private fun editingChart(chart: ChartModel) =
    unsavedChartModel(title = chart.title.toString(), userUid = chart.userId)
