package com.omricat.maplibrarian.model

import kotlinx.serialization.Serializable

public sealed interface ChartModel {
    public val title: String
}

@Serializable
public data class DbChartModel(override val title: String, public val chartId: ChartId) :
    ChartModel

@Serializable public data class UnsavedChartModel(override val title: String) : ChartModel

@Serializable @JvmInline public value class ChartId(public val id: String)
