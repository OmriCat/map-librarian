package com.omricat.maplibrarian.chartlist

import com.omricat.maplibrarian.chartlist.ChartsWorkflowState.ErrorLoadingCharts
import com.omricat.maplibrarian.chartlist.ChartsWorkflowState.RequestData
import com.omricat.maplibrarian.model.DbChartModel
import com.squareup.workflow1.Snapshot
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
public sealed class ChartsWorkflowState {
    @Serializable public data object RequestData : ChartsWorkflowState()

    @Serializable
    public data class ChartsListLoaded(val list: List<DbChartModel>) : ChartsWorkflowState()

    @Serializable public data object AddingItem : ChartsWorkflowState()

    public data class ErrorLoadingCharts(val error: ChartsRepository.Error) : ChartsWorkflowState()

    internal companion object
}

internal fun ChartsWorkflowState.Companion.fromSnapshot(snapshot: Snapshot) =
    Json.decodeFromString(serializer(), snapshot.bytes.utf8())

internal fun ChartsWorkflowState.toSnapshot(): Snapshot =
    Snapshot.of(
        Json.encodeToString(
            ChartsWorkflowState.serializer(),
            if (this is ErrorLoadingCharts) RequestData else this
        )
    )
