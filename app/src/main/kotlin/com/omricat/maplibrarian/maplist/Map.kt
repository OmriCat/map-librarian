package com.omricat.maplibrarian.maplist

data class Map(val mapId: MapId, val title: String)

@JvmInline
value class MapId(val id: String)
