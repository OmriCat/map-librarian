package com.omricat.maplibrarian

/*
    Model classes that are used throughout the app
 */

data class Map(val mapId: MapId, val title: String)

@JvmInline
value class MapId(val id: String)
