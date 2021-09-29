package com.omricat.maplibrarian.model

public interface MapModel {
    public val userId: UserUid
    public val title: CharSequence
}

public data class DbMapModel(
    val mapId: MapId,
    override val title: String,
    override val userId: UserUid
) : MapModel {
    public constructor(mapId: MapId, map: MapModel) : this(
        mapId,
        map.title.toString(),
        map.userId
    )

    public constructor(mapId: MapId, title: CharSequence, userId: UserUid) : this(
        mapId,
        title.toString(),
        userId
    )
}

@JvmInline
public value class MapId(public val id: String)
