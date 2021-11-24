package com.omricat.maplibrarian.chartlist

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.omricat.maplibrarian.chartlist.ActualChartsWorkflow.Props
import com.omricat.maplibrarian.chartlist.ChartsListWorkflow.Event.SelectItem
import com.omricat.maplibrarian.chartlist.ChartsScreen.Loading
import com.omricat.maplibrarian.chartlist.ChartsScreen.ShowError
import com.omricat.maplibrarian.chartlist.ChartsState.AddingItem
import com.omricat.maplibrarian.chartlist.ChartsState.ChartsListLoaded
import com.omricat.maplibrarian.chartlist.ChartsState.ErrorLoadingCharts
import com.omricat.maplibrarian.chartlist.ChartsState.RequestData
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

public interface ChartsWorkflow : Workflow<Props, Nothing, ChartsScreen>

public class ActualChartsWorkflow(
    private val chartsService: ChartsService,
    private val chartAddItemWorkflow: ChartAddItemWorkflow
) : StatefulWorkflow<Props, ChartsState, Nothing, ChartsScreen>(), ChartsWorkflow {

    public data class Props(val user: User)

    override fun initialState(props: Props, snapshot: Snapshot?): ChartsState =
        RequestData

    override fun render(
        renderProps: Props,
        renderState: ChartsState,
        context: RenderContext
    ): ChartsScreen = when (renderState) {
        is RequestData -> {
            context.runningWorker(loadChartList(chartsService, renderProps.user)) { result ->
                result.map { onChartListLoaded(it) }.getOrElse { onLoadingError(it) }
            }
            Loading
        }

        is ChartsListLoaded -> {
            val listScreen = context.renderChild(
                ChartsListWorkflow,
                props = renderState.list
            ) { event ->
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
            context.renderChild(chartAddItemWorkflow, props = renderProps.user) { onItemAdded() }

        is ErrorLoadingCharts -> ShowError(renderState.error.message)
    }

    internal fun onItemAdded() = action { state = RequestData }

    override fun snapshotState(state: ChartsState): Snapshot? =
        null // TODO(#18) Implement snapshots

    internal fun onSelectItem(itemIndex: Int) = action {}

    internal fun onChartListLoaded(list: List<DbChartModel>) =
        action { state = ChartsListLoaded(list) }

    internal fun onLoadingError(error: ChartsServiceError) =
        action { state = ErrorLoadingCharts(error) }

    internal fun onAddItemClicked() =
        action { state = AddingItem }

    internal companion object {

        internal fun loadChartList(
            chartsService: ChartsService,
            user: User
        ): Worker<Result<List<DbChartModel>, ChartsServiceError>> =
            resultWorker(ChartsServiceError::fromThrowable) { chartsService.chartsListForUser(user) }
    }
}

public sealed interface ChartsState {
    public object RequestData : ChartsState
    public data class ChartsListLoaded(val list: List<DbChartModel>) : ChartsState
    public object AddingItem : ChartsState
    public data class ErrorLoadingCharts(val error: ChartsServiceError) : ChartsState
}

public sealed interface ChartsScreen {
    public object Loading : ChartsScreen
    public data class ShowError(val message: String) : ChartsScreen
}

public data class AddItemDecoratorScreen<ChildScreenT : ChartsScreen>(
    val childScreen: ChildScreenT,
    val onAddItemClicked: () -> Unit
) : ChartsScreen
