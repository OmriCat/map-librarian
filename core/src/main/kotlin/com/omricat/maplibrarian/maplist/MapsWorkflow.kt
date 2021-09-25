package com.omricat.maplibrarian.maplist

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.omricat.maplibrarian.maplist.ActualMapsWorkflow.Props
import com.omricat.maplibrarian.maplist.MapsListWorkflow.Event.SelectItem
import com.omricat.maplibrarian.maplist.MapsScreen.Loading
import com.omricat.maplibrarian.maplist.MapsScreen.ShowError
import com.omricat.maplibrarian.maplist.MapsState.AddingItem
import com.omricat.maplibrarian.maplist.MapsState.ErrorLoadingMaps
import com.omricat.maplibrarian.maplist.MapsState.MapListLoaded
import com.omricat.maplibrarian.maplist.MapsState.RequestData
import com.omricat.maplibrarian.model.DbMapModel
import com.omricat.maplibrarian.model.User
import com.omricat.workflow.eventHandler
import com.omricat.workflow.resultWorker
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker

public interface MapsWorkflow : Workflow<Props, Nothing, MapsScreen>

public class ActualMapsWorkflow(
    private val mapsService: MapsService,
    private val mapAddItemWorkflow: MapAddItemWorkflow
) : StatefulWorkflow<Props, MapsState, Nothing, MapsScreen>(), MapsWorkflow {

    public data class Props(val user: User)

    override fun initialState(props: Props, snapshot: Snapshot?): MapsState =
        RequestData

    override fun render(
        renderProps: Props,
        renderState: MapsState,
        context: RenderContext
    ): MapsScreen = when (renderState) {
        is RequestData -> {
            context.runningWorker(loadMapList(mapsService, renderProps.user)) { result ->
                result.map { onMapListLoaded(it) }.getOrElse { onLoadingError(it) }
            }
            Loading
        }

        is MapListLoaded -> {
            val mapListScreen = context.renderChild(
                MapsListWorkflow,
                props = renderState.list
            ) { event ->
                when (event) {
                    is SelectItem -> onSelectItem(event.itemIndex)
                }
            }
            AddItemDecoratorScreen(
                childScreen = mapListScreen,
                onAddItemClicked = context.eventHandler(::onAddItemClicked)
            )
        }
        is AddingItem ->
            context.renderChild(mapAddItemWorkflow, props = renderProps.user) { onItemAdded() }

        is ErrorLoadingMaps -> ShowError(renderState.error.message)
    }

    internal fun onItemAdded() = action { state = RequestData }

    override fun snapshotState(state: MapsState): Snapshot? = null // TODO: Implement snapshots

    internal fun onSelectItem(itemIndex: Int) = action {}

    internal fun onMapListLoaded(list: List<DbMapModel>) =
        action { state = MapListLoaded(list) }

    internal fun onLoadingError(error: MapsServiceError) =
        action { state = ErrorLoadingMaps(error) }

    internal fun onAddItemClicked() =
        action { state = AddingItem }

    internal companion object {

        internal fun loadMapList(
            mapsService: MapsService,
            user: User
        ): Worker<Result<List<DbMapModel>, MapsServiceError>> =
            resultWorker(MapsServiceError::fromThrowable) { mapsService.mapsListForUser(user) }
    }
}

public sealed interface MapsState {
    public object RequestData : MapsState
    public data class MapListLoaded(val list: List<DbMapModel>) : MapsState
    public object AddingItem : MapsState
    public data class ErrorLoadingMaps(val error: MapsServiceError) : MapsState
}

public sealed interface MapsScreen {
    public object Loading : MapsScreen
    public data class ShowError(val message: String) : MapsScreen
}

public data class AddItemDecoratorScreen<ChildScreenT : MapsScreen>(
    val childScreen: ChildScreenT,
    val onAddItemClicked: () -> Unit
) : MapsScreen
