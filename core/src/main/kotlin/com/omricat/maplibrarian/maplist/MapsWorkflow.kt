package com.omricat.maplibrarian.maplist

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.omricat.maplibrarian.maplist.ActualMapsWorkflow.Props
import com.omricat.maplibrarian.maplist.MapsListWorkflow.Event
import com.omricat.maplibrarian.maplist.MapsScreen.Loading
import com.omricat.maplibrarian.maplist.MapsScreen.ShowError
import com.omricat.maplibrarian.maplist.MapsState.ErrorLoadingMaps
import com.omricat.maplibrarian.maplist.MapsState.MapListLoaded
import com.omricat.maplibrarian.maplist.MapsState.RequestData
import com.omricat.maplibrarian.model.Map
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.root.AuthorizedProps
import com.omricat.workflow.resultWorker
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker

public interface MapsWorkflow : Workflow<Props, Nothing, MapsScreen>

public class ActualMapsWorkflow(private val mapListService: MapListService) :
    StatefulWorkflow<Props, MapsState, Nothing, MapsScreen>(), MapsWorkflow {

    public data class Props(override val user: User) : AuthorizedProps

    override fun initialState(props: Props, snapshot: Snapshot?): MapsState =
        RequestData

    override fun render(
        renderProps: Props,
        renderState: MapsState,
        context: RenderContext
    ): MapsScreen = when (renderState) {
        is RequestData -> {
            context.runningWorker(loadMapList(mapListService, renderProps.user)) { result ->
                result.map { onMapListLoaded(it) }.getOrElse { onLoadingError(it) }
            }
            Loading
        }
        is MapListLoaded -> context.renderChild(
            MapsListWorkflow,
            props = renderState.list
        ) { event ->
            when (event) {
                is Event.SelectItem -> onSelectItem(event.itemIndex)
            }
        }
        is ErrorLoadingMaps -> ShowError(renderState.error.message)
    }

    private fun onSelectItem(itemIndex: Int) = action {}

    override fun snapshotState(state: MapsState): Snapshot? = null // TODO: Implement snapshots

    internal fun onMapListLoaded(list: List<Map>) = action { state = MapListLoaded(list) }

    internal fun onLoadingError(error: MapListError) = action { state = ErrorLoadingMaps(error) }

    internal companion object {

        internal fun loadMapList(
            mapListService: MapListService,
            user: User
        ): Worker<Result<List<Map>, MapListError>> =
            resultWorker(MapListError::fromThrowable) { mapListService.mapsListForUser(user) }
    }
}

public sealed class MapsState {
    public object RequestData : MapsState()
    public data class MapListLoaded(val list: List<Map>) : MapsState()
    public data class ErrorLoadingMaps(val error: MapListError) : MapsState()
}

public sealed interface MapsScreen {
    public object Loading : MapsScreen
    public data class ShowError(val message: String) : MapsScreen
}
