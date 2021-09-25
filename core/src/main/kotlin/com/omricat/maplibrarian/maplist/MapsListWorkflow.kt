package com.omricat.maplibrarian.maplist

import com.omricat.maplibrarian.maplist.MapsListWorkflow.Event
import com.omricat.maplibrarian.maplist.MapsListWorkflow.Event.SelectItem
import com.omricat.maplibrarian.model.DbMapModel
import com.omricat.workflow.eventHandler
import com.squareup.workflow1.StatelessWorkflow
import com.squareup.workflow1.action

public object MapsListWorkflow : StatelessWorkflow<List<DbMapModel>, Event, MapListScreen>() {
    public sealed interface Event {
        public data class SelectItem(val itemIndex: Int) : Event
    }

    override fun render(renderProps: List<DbMapModel>, context: RenderContext): MapListScreen =
        MapListScreen(
            list = renderProps,
            onItemSelect = context.eventHandler(::onSelectItem)
        )

    internal fun onSelectItem(index: Int) = action {
        setOutput(SelectItem(index))
    }
}

public data class MapListScreen(val list: List<DbMapModel>, val onItemSelect: (Int) -> Unit) :
    MapsScreen
