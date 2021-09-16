package com.omricat.maplibrarian.maplist

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.omricat.maplibrarian.Map
import com.omricat.maplibrarian.User
import com.omricat.maplibrarian.maplist.MapListScreen.Loading
import com.omricat.maplibrarian.maplist.MapListScreen.MapList
import com.omricat.maplibrarian.maplist.MapListScreen.ShowError
import com.omricat.maplibrarian.maplist.MapListState.ErrorLoadingMaps
import com.omricat.maplibrarian.maplist.MapListState.MapListLoaded
import com.omricat.maplibrarian.maplist.MapListState.RequestData
import com.omricat.maplibrarian.maplist.MapListWorkflow.Output
import com.omricat.maplibrarian.maplist.MapListWorkflow.Output.LogOut
import com.omricat.maplibrarian.maplist.MapListWorkflow.Props
import com.omricat.workflow.asResultWorker
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker

class MapListWorkflow(private val mapListService: MapListService) :
    StatefulWorkflow<Props, MapListState, Output, MapListScreen>() {

    sealed interface Output {
        object LogOut : Output
    }

    data class Props(val user: User)

    override fun initialState(props: Props, snapshot: Snapshot?): MapListState =
        RequestData

    override fun render(
        renderProps: Props,
        renderState: MapListState,
        context: RenderContext
    ): MapListScreen = when (renderState) {
        is RequestData -> {
            context.runningWorker(loadMapList(renderProps.user)) { result ->
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

    private fun loadMapList(user: User): Worker<Result<List<Map>, MapListError>> =
        mapListService::mapsListForUser.asResultWorker(errorWrapper = MapListError::fromThrowable)
            .invoke(user)

    override fun snapshotState(state: MapListState): Snapshot? = null // TODO: Implement snapshots
}

sealed class MapListState {
    object RequestData : MapListState()
    data class MapListLoaded(val list: List<Map>) : MapListState()
    data class ErrorLoadingMaps(val message: String) : MapListState()
}

sealed class MapListScreen {
    object Loading : MapListScreen()
    data class MapList(val list: List<Map>, val logOutCmd: () -> Unit) : MapListScreen()
    data class ShowError(val message: String) : MapListScreen()
}
