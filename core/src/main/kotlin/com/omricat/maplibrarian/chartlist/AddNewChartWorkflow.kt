package com.omricat.maplibrarian.chartlist

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.omricat.maplibrarian.chartlist.AddNewChartWorkflow.Event
import com.omricat.maplibrarian.chartlist.AddNewChartWorkflow.Event.Discard
import com.omricat.maplibrarian.chartlist.AddNewChartWorkflow.Event.Saved
import com.omricat.maplibrarian.chartlist.AddNewChartWorkflow.Props
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

public class AddNewChartWorkflow(private val chartsService: ChartsService) :
    StatefulWorkflow<Props, State, Event, EditingItemScreen>() {
    public data class Props(
        val user: User,
        val initialChart: ChartModel<*> = emptyChartModel(user)
    )

    @Serializable
    public sealed class State {
        @Serializable
        public data class Editing<T : ChartModel<T>>(
            val chart: ChartModel<T>,
            val errorMessage: String = ""
        ) : State()

        @Serializable
        public data class Saving<T : ChartModel<T>>(val chart: ChartModel<T>) : State()

        internal companion object {
            internal fun fromSnapshot(snapshot: Snapshot): State =
                Json.decodeFromString(serializer(), snapshot.bytes.utf8())
        }
    }

    public sealed interface Event {
        public object Discard : Event
        public data class Saved(val savedChart: DbChartModel) : Event
    }

    override fun initialState(props: Props, snapshot: Snapshot?): State =
        snapshot?.let(State::fromSnapshot) ?: Editing(props.initialChart)

    override fun render(
        renderProps: Props,
        renderState: State,
        context: RenderContext
    ): EditingItemScreen = when (renderState) {
        is Editing<*> -> {
            EditItemScreen(
                chart = renderState.chart,
                errorMessage = renderState.errorMessage,
                onTitleChanged = context.eventHandler(onTitleChanged(renderState.chart)),
                discardChanges = context.eventHandler(::onDiscard),
                saveChanges = context.eventHandler(onSave(renderState.chart))
            )
        }
        is Saving<*> -> {
            context.runningWorker(saveChart(renderProps, renderState.chart)) { result ->
                result.map { savedChart -> onNewItemSaved(savedChart) }
                    .getOrElse { e -> onErrorSaving(renderState.chart, e) }
            }
            SavingItemScreen(renderState.chart)
        }
    }

    override fun snapshotState(state: State): Snapshot = state.toSnapshot()

    internal fun onErrorSaving(
        chart: ChartModel<*>,
        e: ChartsServiceError
    ) = action {
        state = Editing(chart, errorMessage = e.message)
    }

    internal fun onNewItemSaved(savedChart: DbChartModel) = action { setOutput(Saved(savedChart)) }

    private fun <T : ChartModel<T>> saveChart(
        props: Props,
        chart: ChartModel<T>
    ): Worker<Result<DbChartModel, ChartsServiceError>> =
        resultWorker(ChartsServiceError::fromThrowable) {
            chartsService.saveChart(props.user, chart)
        }

    private fun <T : ChartModel<T>> onSave(chart: ChartModel<T>) = action { state = Saving(chart) }

    private fun onDiscard() = action { setOutput(Discard) }

    private fun onTitleChanged(chart: ChartModel<*>) = { newTitle: CharSequence ->
        action { state = Editing(chart.clone(title = newTitle.toString())) }
    }
}

internal fun State.toSnapshot(): Snapshot =
    Snapshot.of(Json.encodeToString(State.serializer(), this))

private fun emptyChartModel(user: User) =
    UnsavedChartModel(user.id, "")

public sealed interface EditingItemScreen : ChartsScreen {
    public val chart: ChartModel<*>
}

public data class EditItemScreen(
    override val chart: ChartModel<*>,
    val errorMessage: String = "",
    val onTitleChanged: (CharSequence) -> Unit,
    val discardChanges: () -> Unit,
    val saveChanges: () -> Unit
) : EditingItemScreen

public data class SavingItemScreen(override val chart: ChartModel<*>) : EditingItemScreen
