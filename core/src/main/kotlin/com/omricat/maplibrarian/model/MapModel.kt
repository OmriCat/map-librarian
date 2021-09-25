package com.omricat.maplibrarian.model

public interface MapModel {
    public val title: CharSequence
}

public data class DbMapModel(val mapId: MapId, override val title: CharSequence) : MapModel {
    public constructor(mapId: MapId, map: MapModel) : this(mapId, map.title)
}

@JvmInline
public value class MapId(public val id: String)
