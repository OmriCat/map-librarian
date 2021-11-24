package com.omricat.maplibrarian.model

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.map
import com.omricat.maplibrarian.model.DbChartModelDeserializer.Error
import com.omricat.maplibrarian.model.MapModelProperties.TITLE
import com.omricat.maplibrarian.model.MapModelProperties.USER_ID
import com.omricat.maplibrarian.model.serialization.IdDeserializer
import com.omricat.maplibrarian.model.serialization.getProperty
import com.omricat.maplibrarian.model.UserUid as UserId

private object MapModelProperties {

    const val TITLE = "title"
    const val USER_ID = "userId"
}

public object ChartModelSerializer : Serializer<ChartModel> {
    override operator fun invoke(model: ChartModel): Map<String, String> =
        hashMapOf(
            TITLE to model.title.toString(),
            USER_ID to model.userId.id
        )
}

public fun ChartModel.serialized(): Map<String, Any?> =
    ChartModelSerializer(this)

public object DbChartModelDeserializer : IdDeserializer<DbChartModel, Error> {
    override operator fun invoke(
        id: String,
        properties: Map<String, Any?>
    ): Result<DbChartModel, Error> = binding {
        val mapId = ChartId(id)
        val userId: UserId = properties.getProperty<String>(USER_ID).map { UserId(it) }.bind()
        val title: String = properties.getProperty<String>(TITLE).bind()
        DbChartModel(userId = userId, id = mapId, title = title)
    }

    public data class Error(val message: String)
}
