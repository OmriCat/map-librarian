package com.omricat.maplibrarian.maplist

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.omricat.maplibrarian.maplist.MapListScreen.Loading
import com.omricat.maplibrarian.maplist.MapListScreen.MapList
import com.omricat.maplibrarian.maplist.MapListScreen.ShowError
import com.omricat.maplibrarian.maplist.MapListState.ErrorLoadingMaps
import com.omricat.maplibrarian.maplist.MapListState.MapListLoaded
import com.omricat.maplibrarian.maplist.MapListState.RequestData
import com.omricat.maplibrarian.maplist.MapListWorkflow.Output
import com.omricat.maplibrarian.maplist.MapListWorkflow.Output.LogOut
import com.omricat.maplibrarian.maplist.MapListWorkflow.Props
import com.omricat.maplibrarian.model.Map
import com.omricat.maplibrarian.model.User
import com.omricat.workflow.resultWorker
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker

public class MapListWorkflow(private val mapListService: MapListService) :
    StatefulWorkflow<Props, MapListState, Output, MapListScreen>() {

    public sealed interface Output {
        public object LogOut : Output
    }

    public data class Props(val user: User)

    override fun initialState(props: Props, snapshot: Snapshot?): MapListState =
        RequestData

    override fun render(
        renderProps: Props,
        renderState: MapListState,
        context: RenderContext
    ): MapListScreen = when (renderState) {
        is RequestData -> {
            context.runningWorker(loadMapList(mapListService, renderProps.user)) { result ->
                action {
                    this.state = result.map { MapListLoaded(it) }.getOrElse { ErrorLoadingMaps("") }
                }
            }
            Loading
        }
        is MapListLoaded -> MapList(
            list = renderState.list,
            logOutCmd = { context.actionSink.send(action { setOutput(LogOut) }) }
        )
        is ErrorLoadingMaps -> ShowError(renderState.message)
    }

    override fun snapshotState(state: MapListState): Snapshot? = null // TODO: Implement snapshots

    private companion object {
        private fun loadMapList(
            mapListService: MapListService,
            user: User
        ): Worker<Result<List<Map>, MapListError>> =
            resultWorker(MapListError::fromThrowable) { mapListService.mapsListForUser(user) }
    }
}

public sealed class MapListState {
    public object RequestData : MapListState()
    public data class MapListLoaded(val list: List<Map>) : MapListState()
    public data class ErrorLoadingMaps(val message: String) : MapListState()
}

public sealed class MapListScreen {
    public object Loading : MapListScreen()
    public data class MapList(val list: List<Map>, val logOutCmd: () -> Unit) : MapListScreen()
    public data class ShowError(val message: String) : MapListScreen()
}
