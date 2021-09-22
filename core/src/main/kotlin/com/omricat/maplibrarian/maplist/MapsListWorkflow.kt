package com.omricat.maplibrarian.maplist

import com.omricat.maplibrarian.maplist.MapsListWorkflow.Event
import com.omricat.maplibrarian.maplist.MapsListWorkflow.Event.SelectItem
import com.omricat.maplibrarian.model.Map
import com.squareup.workflow1.StatelessWorkflow
import com.squareup.workflow1.WorkflowAction
import com.squareup.workflow1.action

public object MapsListWorkflow : StatelessWorkflow<List<Map>, Event, MapListScreen>() {
    public sealed interface Event {
        public data class SelectItem(val itemIndex: Int) : Event
    }

    override fun render(renderProps: List<Map>, context: RenderContext): MapListScreen =
        MapListScreen(
            list = renderProps,
            onItemSelect = { context.actionSink.send(onSelectItem(it)) }
        )

    internal fun onSelectItem(index: Int): WorkflowAction<List<Map>, Nothing, Event> = action {
        setOutput(SelectItem(index))
    }
}

public data class MapListScreen(val list: List<Map>, val onItemSelect: (Int) -> Unit) : MapsScreen
