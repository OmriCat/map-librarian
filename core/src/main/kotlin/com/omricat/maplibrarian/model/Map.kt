package com.omricat.maplibrarian.model

public data class Map(val mapId: MapId, val title: String)

@JvmInline
public value class MapId(public val id: String)
