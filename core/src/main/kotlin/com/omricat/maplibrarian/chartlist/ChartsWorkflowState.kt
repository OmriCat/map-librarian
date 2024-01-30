package com.omricat.maplibrarian.chartlist

import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.maplibrarian.utils.Snapshotter
import kotlinx.serialization.Serializable
import kotlinx.serialization.StringFormat

@Serializable
public sealed class ChartsWorkflowState {
    @Serializable public data object RequestData : ChartsWorkflowState()

    @Serializable
    public data class ChartsListLoaded(val list: List<DbChartModel>) : ChartsWorkflowState()

    @Serializable public data object AddingItem : ChartsWorkflowState()

    public data class ErrorLoadingCharts(val error: ChartsRepository.Error) : ChartsWorkflowState()

    internal companion object {
        fun snapshotter(stringFormat: StringFormat): Snapshotter<ChartsWorkflowState> =
            object :
                Snapshotter<ChartsWorkflowState>(
                    stringFormat,
                    serializer(),
                ) {
                override fun preSerialization(value: ChartsWorkflowState): ChartsWorkflowState =
                    if (value is ErrorLoadingCharts) RequestData else value
            }
    }
}
