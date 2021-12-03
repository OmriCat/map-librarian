package com.omricat.maplibrarian.model

import kotlinx.serialization.Serializable

@Serializable
public data class ChartModel<IdT : ChartId?>(
    public val userId: UserUid,
    public val title: String,
    public val chartId: IdT
)

public typealias DbChartModel = ChartModel<ChartId>

@Serializable
@JvmInline
public value class ChartId(public val id: String)
