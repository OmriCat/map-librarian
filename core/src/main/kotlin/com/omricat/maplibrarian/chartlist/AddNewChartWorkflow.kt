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
import com.omricat.workflow.eventHandler
import com.omricat.workflow.resultWorker
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

public class AddNewChartWorkflow(private val chartsRepository: ChartsRepository) :
    StatefulWorkflow<User, State, Event, AddingItemScreen>() {

    @Serializable
    public sealed class State {

        @Serializable
        public data class Editing(val chart: UnsavedChartModel, val errorMessage: String = "") :
            State()

        @Serializable public data class Saving(val chart: UnsavedChartModel) : State()

        internal companion object {
            internal fun fromSnapshot(snapshot: Snapshot): State =
                Json.decodeFromString(serializer(), snapshot.bytes.utf8())
        }
    }

    public sealed interface Event {
        public object Discard : Event

        public object Saved : Event
    }

    override fun initialState(props: User, snapshot: Snapshot?): State =
        snapshot?.let(State::fromSnapshot) ?: Editing(UnsavedChartModel(props.id, ""))

    override fun render(
        renderProps: User,
        renderState: State,
        context: RenderContext
    ): AddingItemScreen =
        when (renderState) {
            is Editing -> {
                AddItemScreen(
                    chart = renderState.chart,
                    errorMessage = renderState.errorMessage,
                    onTitleChanged = context.eventHandler(onTitleChanged(renderState.chart)),
                    discardChanges = context.eventHandler(::onDiscard),
                    saveChanges = context.eventHandler(onSave(renderState.chart))
                )
            }
            is Saving -> {
                context.runningWorker(saveNewItem(renderProps, renderState.chart)) { result ->
                    result
                        .map { savedChart -> onNewItemSaved(savedChart) }
                        .getOrElse { e -> onErrorSaving(renderState.chart, e) }
                }
                SavingItemScreen(renderState.chart)
            }
        }

    override fun snapshotState(state: State): Snapshot = state.toSnapshot()

    internal fun onErrorSaving(chart: UnsavedChartModel, e: ChartsRepository.Error) = action {
        state = Editing(chart, errorMessage = e.message)
    }

    internal fun onNewItemSaved(savedChart: DbChartModel) = action { setOutput(Saved) }

    private fun saveNewItem(
        user: User,
        chart: UnsavedChartModel
    ): Worker<Result<DbChartModel, ChartsRepository.Error>> =
        resultWorker(ChartsServiceError::fromThrowable) {
            chartsRepository.addNewChart(user, chart)
        }

    private fun onSave(chart: UnsavedChartModel) = action { state = Saving(chart) }

    private fun onDiscard() = action { setOutput(Discard) }

    private fun onTitleChanged(chart: UnsavedChartModel) = { newTitle: CharSequence ->
        action { state = Editing(chart.copy(title = newTitle.toString())) }
    }
}

internal fun State.toSnapshot(): Snapshot =
    Snapshot.of(Json.encodeToString(State.serializer(), this))

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
