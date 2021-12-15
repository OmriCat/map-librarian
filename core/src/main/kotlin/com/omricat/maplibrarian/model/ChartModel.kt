package com.omricat.maplibrarian.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

public sealed interface ChartModel<SelfT : ChartModel<SelfT>> {
    public val userId: UserUid
    public val title: String
    public fun clone(title: String = this.title): SelfT

    public companion object {

        public val serializerModule: SerializersModule = SerializersModule {
            polymorphic(ChartModel::class) {
                subclass(DbChartModel::class)
                subclass(UnsavedChartModel::class)
            }
        }
    }
}

@Serializable
public data class DbChartModel(
    override val userId: UserUid,
    override val title: String,
    public val chartId: ChartId
) : ChartModel<DbChartModel> {
    override fun clone(title: String): DbChartModel = copy(title = title)
}

@Serializable
public data class UnsavedChartModel(
    override val userId: UserUid,
    override val title: String
) : ChartModel<UnsavedChartModel> {
    override fun clone(title: String): UnsavedChartModel = copy(title = title)
}

@Serializable
@JvmInline
public value class ChartId(public val id: String)
