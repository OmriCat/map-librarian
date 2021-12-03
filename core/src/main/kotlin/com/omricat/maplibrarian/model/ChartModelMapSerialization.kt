package com.omricat.maplibrarian.model

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.map
import com.omricat.maplibrarian.model.MapModelProperties.TITLE
import com.omricat.maplibrarian.model.MapModelProperties.USER_ID
import com.omricat.maplibrarian.model.serialization.DeserializerError
import com.omricat.maplibrarian.model.serialization.FromMapWithIdDeserializer
import com.omricat.maplibrarian.model.serialization.ToMapSerializer
import com.omricat.maplibrarian.model.serialization.getProperty
import com.omricat.maplibrarian.model.UserUid as UserId

private object MapModelProperties {

    const val TITLE = "title"
    const val USER_ID = "userId"
}

public object ChartModelToMapSerializer : ToMapSerializer<UnsavedChartModel> {
    override operator fun invoke(model: UnsavedChartModel): Map<String, String> =
        hashMapOf(
            TITLE to model.title,
            USER_ID to model.userId.id
        )
}

public fun UnsavedChartModel.serializedToMap(): Map<String, Any?> =
    ChartModelToMapSerializer(this)

public object DbChartModelFromMapDeserializer :
    FromMapWithIdDeserializer<DbChartModel, DeserializerError> {
    override operator fun invoke(
        id: String,
        properties: Map<String, Any?>
    ): Result<DbChartModel, DeserializerError> = binding {
        val mapId = ChartId(id)
        val userId = properties.getProperty<String>(USER_ID).map { UserId(it) }.bind()
        val title = properties.getProperty<String>(TITLE).bind()
        DbChartModel(userId = userId, chartId = mapId, title = title)
    }
}
