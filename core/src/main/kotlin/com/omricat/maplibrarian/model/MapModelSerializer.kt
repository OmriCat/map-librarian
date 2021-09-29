package com.omricat.maplibrarian.model

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.map
import com.omricat.maplibrarian.model.DbMapModelDeserializer.Error
import com.omricat.maplibrarian.model.MapModelProperties.TITLE
import com.omricat.maplibrarian.model.MapModelProperties.USER_ID
import com.omricat.maplibrarian.model.serialization.IdDeserializer
import com.omricat.maplibrarian.model.serialization.getProperty
import com.omricat.maplibrarian.model.UserUid as UserId

private object MapModelProperties {

    const val TITLE = "title"
    const val USER_ID = "userId"
}

public object MapModelSerializer : Serializer<MapModel> {
    override operator fun invoke(mapModel: MapModel): Map<String, Any?> =
        hashMapOf(
            TITLE to mapModel.title,
            USER_ID to mapModel.userId
        )
}

public fun MapModel.serialized(): Map<String, Any?> =
    MapModelSerializer(this)

public object DbMapModelDeserializer : IdDeserializer<DbMapModel, Error> {
    override operator fun invoke(
        id: String,
        properties: Map<String, Any?>
    ): Result<DbMapModel, Error> = binding {
        val mapId = MapId(id)
        val userId: UserId = properties.getProperty<String>(USER_ID).map { UserId(it) }.bind()
        val title: String = properties.getProperty<String>(TITLE).bind()
        DbMapModel(userId = userId, mapId = mapId, title = title)
    }

    public data class Error(val message: String)
}
