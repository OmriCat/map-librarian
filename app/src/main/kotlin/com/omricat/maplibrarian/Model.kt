package com.omricat.maplibrarian

import com.google.firebase.auth.FirebaseUser

/*
    Model classes that are used throughout the app
 */

data class Map(val mapId: MapId, val title: String)

@JvmInline
value class MapId(val id: String)

@JvmInline
value class User(private val user: FirebaseUser) {
    val displayName: String
        get() = user.displayName ?: "(unknown name)"

    val id: String
        get() = user.uid
}
