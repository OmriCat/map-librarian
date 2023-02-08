package com.omricat.maplibrarian.chartlist

import com.omricat.maplibrarian.chartlist.ChartsListWorkflow.Event
import com.omricat.maplibrarian.chartlist.ChartsListWorkflow.Event.SelectItem
import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.workflow.eventHandler
import com.squareup.workflow1.StatelessWorkflow
import com.squareup.workflow1.action

public object ChartsListWorkflow : StatelessWorkflow<List<DbChartModel>, Event, ChartListScreen>() {
    public sealed interface Event {
        public data class SelectItem(val itemIndex: Int) : Event
    }

    override fun render(renderProps: List<DbChartModel>, context: RenderContext): ChartListScreen =
        ChartListScreen(list = renderProps, onItemSelect = context.eventHandler(::onSelectItem))

    internal fun onSelectItem(index: Int) = action { setOutput(SelectItem(index)) }
}

public data class ChartListScreen(val list: List<DbChartModel>, val onItemSelect: (Int) -> Unit) :
    ChartsScreen
