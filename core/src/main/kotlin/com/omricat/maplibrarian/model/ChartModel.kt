package com.omricat.maplibrarian.model

public data class ChartModel<IdT : ChartId?>(
    public val userId: UserUid,
    public val title: CharSequence,
    public val chartId: IdT
)

public typealias DbChartModel = ChartModel<ChartId>

@JvmInline
public value class ChartId(public val id: String)
