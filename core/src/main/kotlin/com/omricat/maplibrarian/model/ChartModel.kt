package com.omricat.maplibrarian.model

public interface ChartModel {
    public val userId: UserUid
    public val title: CharSequence
}

public data class DbChartModel(
    val id: ChartId,
    override val title: String,
    override val userId: UserUid
) : ChartModel {
    public constructor(chartId: ChartId, chart: ChartModel) : this(
        chartId,
        chart.title.toString(),
        chart.userId
    )

    public constructor(chartId: ChartId, title: CharSequence, userId: UserUid) : this(
        chartId,
        title.toString(),
        userId
    )
}

@JvmInline
public value class ChartId(public val id: String)
