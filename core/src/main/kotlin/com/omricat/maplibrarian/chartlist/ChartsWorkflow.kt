package com.omricat.maplibrarian.chartlist

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.omricat.maplibrarian.chartlist.ChartsListWorkflow.Event.SelectItem
import com.omricat.maplibrarian.chartlist.ChartsRepository.Error.ExceptionWrappingError
import com.omricat.maplibrarian.chartlist.ChartsScreen.Loading
import com.omricat.maplibrarian.chartlist.ChartsScreen.ShowError
import com.omricat.maplibrarian.chartlist.ChartsWorkflow.Props
import com.omricat.maplibrarian.chartlist.ChartsWorkflowState.AddingItem
import com.omricat.maplibrarian.chartlist.ChartsWorkflowState.ChartsListLoaded
import com.omricat.maplibrarian.chartlist.ChartsWorkflowState.ErrorLoadingCharts
import com.omricat.maplibrarian.chartlist.ChartsWorkflowState.RequestData
import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.maplibrarian.model.User
import com.omricat.workflow.eventHandler
import com.omricat.workflow.resultWorker
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import kotlinx.serialization.StringFormat

public interface ChartsWorkflow : Workflow<Props, Nothing, ChartsScreen> {

    public companion object {
        public fun instance(
            chartsRepository: ChartsRepository,
            addNewChartWorkflow: AddNewChartWorkflow,
            stringFormat: StringFormat
        ): ChartsWorkflow = ChartsWorkflowImpl(chartsRepository, addNewChartWorkflow, stringFormat)
    }

    public data class Props(val user: User)
}

private class ChartsWorkflowImpl(
    private val chartsRepository: ChartsRepository,
    private val addNewChartWorkflow: AddNewChartWorkflow,
    stringFormat: StringFormat
) : StatefulWorkflow<Props, ChartsWorkflowState, Nothing, ChartsScreen>(), ChartsWorkflow {

    private val snapshotter = ChartsWorkflowState.snapshotter(stringFormat)

    override fun initialState(props: Props, snapshot: Snapshot?): ChartsWorkflowState =
        snapshot?.let { snapshotter.valueFromSnapshot(it) } ?: RequestData

    override fun render(
        renderProps: Props,
        renderState: ChartsWorkflowState,
        context: RenderContext
    ): ChartsScreen =
        when (renderState) {
            is RequestData -> {
                context.runningWorker(loadChartList(chartsRepository, renderProps.user)) { result ->
                    result.map { onChartListLoaded(it) }.getOrElse { onLoadingError(it) }
                }
                Loading
            }
            is ChartsListLoaded -> {
                val listScreen =
                    context.renderChild(ChartsListWorkflow, props = renderState.list) { event ->
                        when (event) {
                            is SelectItem -> onSelectItem(event.itemIndex)
                        }
                    }
                AddItemDecoratorScreen(
                    childScreen = listScreen,
                    onAddItemClicked = context.eventHandler(::onAddItemClicked)
                )
            }
            is AddingItem ->
                context.renderChild(addNewChartWorkflow, props = renderProps.user) { onItemAdded() }
            is ErrorLoadingCharts -> ShowError(renderState.error.message)
        }

    override fun snapshotState(state: ChartsWorkflowState): Snapshot = snapshotter.snapshotOf(state)

    internal fun onItemAdded() = action { state = RequestData }

    internal fun onSelectItem(itemIndex: Int) = action {}

    internal fun onChartListLoaded(list: List<DbChartModel>) = action {
        state = ChartsListLoaded(list)
    }

    internal fun onLoadingError(error: ChartsRepository.Error) = action {
        state = ErrorLoadingCharts(error)
    }

    internal fun onAddItemClicked() = action { state = AddingItem }

    internal companion object {

        internal fun loadChartList(
            chartsRepository: ChartsRepository,
            user: User
        ): Worker<Result<List<DbChartModel>, ChartsRepository.Error>> =
            resultWorker(::ExceptionWrappingError) { chartsRepository.chartsListForUser(user) }
    }
}

public sealed interface ChartsScreen {
    public data object Loading : ChartsScreen

    public data class ShowError(val message: String) : ChartsScreen
}

public data class AddItemDecoratorScreen<ChildScreenT : ChartsScreen>(
    val childScreen: ChildScreenT,
    val onAddItemClicked: () -> Unit
) : ChartsScreen
