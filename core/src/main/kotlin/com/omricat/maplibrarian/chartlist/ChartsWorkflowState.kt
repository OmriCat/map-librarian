package com.omricat.maplibrarian.chartlist

import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.maplibrarian.utils.Snapshotter
import kotlinx.serialization.Serializable
import kotlinx.serialization.StringFormat

@Serializable
internal sealed class ChartsWorkflowState {
    @Serializable data object RequestData : ChartsWorkflowState()

    @Serializable data class ChartsListLoaded(val list: List<DbChartModel>) : ChartsWorkflowState()

    @Serializable data object AddingItem : ChartsWorkflowState()

    data class ErrorLoadingCharts(val error: ChartsRepository.Error) : ChartsWorkflowState()

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
