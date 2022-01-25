package com.omricat.maplibrarian.chartlist

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.omricat.maplibrarian.chartlist.EditChartDetailsWorkflow.Actions.OnDiscard
import com.omricat.maplibrarian.chartlist.EditChartDetailsWorkflow.Actions.OnErrorSaving
import com.omricat.maplibrarian.chartlist.EditChartDetailsWorkflow.Actions.OnNewItemSaved
import com.omricat.maplibrarian.chartlist.EditChartDetailsWorkflow.Actions.OnSave
import com.omricat.maplibrarian.chartlist.EditChartDetailsWorkflow.Actions.OnTitleChanged
import com.omricat.maplibrarian.chartlist.EditChartDetailsWorkflow.Event
import com.omricat.maplibrarian.chartlist.EditChartDetailsWorkflow.Event.Discard
import com.omricat.maplibrarian.chartlist.EditChartDetailsWorkflow.Event.Saved
import com.omricat.maplibrarian.chartlist.EditChartDetailsWorkflow.Props
import com.omricat.maplibrarian.chartlist.EditChartDetailsWorkflow.State
import com.omricat.maplibrarian.chartlist.EditChartDetailsWorkflow.State.Editing
import com.omricat.maplibrarian.chartlist.EditChartDetailsWorkflow.State.Saving
import com.omricat.maplibrarian.model.ChartModel
import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.maplibrarian.model.UnsavedChartModel
import com.omricat.maplibrarian.model.User
import com.omricat.workflow.AbstractWorkflowAction
import com.omricat.workflow.StateSnapshotSerializer
import com.omricat.workflow.StatefulWorkflowWithSnapshots
import com.omricat.workflow.eventHandler
import com.omricat.workflow.resultWorker
import com.squareup.workflow1.Worker
import com.squareup.workflow1.WorkflowAction
import com.squareup.workflow1.runningWorker
import kotlinx.serialization.Serializable
import kotlinx.serialization.StringFormat

public class EditChartDetailsWorkflow(
    private val chartsService: ChartsService,
    serializationFormat: StringFormat = StateSnapshotSerializer.json
) :
    StatefulWorkflowWithSnapshots<Props, State, Event, EditingItemScreen>() {
    public data class Props(
        val user: User,
        val initialChart: ChartModel<*> = UnsavedChartModel(user.id, "")
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
            internal fun snapshotSerializer(format: StringFormat) =
                StateSnapshotSerializer(serializer(), format)
        }
    }

    override val stateSnapshotSerializer: StateSnapshotSerializer<State> =
        State.snapshotSerializer(serializationFormat)

    public sealed interface Event {
        public object Discard : Event
        public data class Saved(val savedChart: DbChartModel) : Event
    }

    override fun initialState(props: Props): State =
        Editing(props.initialChart)

    override fun render(
        renderProps: Props,
        renderState: State,
        context: RenderContext
    ): EditingItemScreen = when (renderState) {
        is Editing<*> -> {
            EditChartDetailsScreen(
                chart = renderState.chart,
                errorMessage = renderState.errorMessage,
                onTitleChanged = { newTitle ->
                    context.actionSink.send(
                        OnTitleChanged(renderState.chart, newTitle)
                    )
                },
                discardChanges = context.eventHandler(OnDiscard),
                saveChanges = context.eventHandler(OnSave(renderState.chart))
            )
        }
        is Saving<*> -> {
            context.runningWorker(saveChart(renderProps, renderState.chart)) { result ->
                result.map { savedChart -> OnNewItemSaved(savedChart) }
                    .getOrElse { e -> OnErrorSaving(renderState.chart, e) }
            }
            SavingItemScreen(renderState.chart)
        }
    }

    internal object Actions {
        class OnErrorSaving(
            private val chart: ChartModel<*>,
            private val error: ChartsServiceError
        ) : Action("OnErrorSaving", {
            state = Editing(chart, errorMessage = error.message)
        })

        class OnNewItemSaved(private val savedChart: DbChartModel) : Action("OnNewItemSaved",
            {
                setOutput(Saved(savedChart))
            })

        object OnDiscard : Action("OnDiscard", { setOutput(Discard) })

        class OnTitleChanged(private val chart: ChartModel<*>, private val newTitle: CharSequence) :
            Action("OnTitleChanged", {
                state = Editing(
                    chart.clone(title = newTitle.toString())
                )
            })

        class OnSave<T : ChartModel<T>>(private val chart: ChartModel<T>) : Action("OnSave", {
            state = Saving(chart)
        })
    }

    internal open class Action(
        name: String,
        updater: WorkflowAction<Props, State, Event>.Updater.() -> Unit
    ) : AbstractWorkflowAction<Props, State, Event>({ name }, updater)

    private fun <T : ChartModel<T>> saveChart(
        props: Props,
        chart: ChartModel<T>
    ): Worker<Result<DbChartModel, ChartsServiceError>> =
        resultWorker(ChartsServiceError::fromThrowable) {
            chartsService.saveChart(props.user, chart)
        }
}

public sealed interface EditingItemScreen : ChartsScreen {
    public val chart: ChartModel<*>
}

public data class EditChartDetailsScreen(
    override val chart: ChartModel<*>,
    val errorMessage: String = "",
    val onTitleChanged: (CharSequence) -> Unit,
    val discardChanges: () -> Unit,
    val saveChanges: () -> Unit
) : EditingItemScreen

public data class SavingItemScreen(override val chart: ChartModel<*>) : EditingItemScreen
