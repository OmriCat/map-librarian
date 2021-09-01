package com.omricat.maplibrarian.auth

sealed interface User {
    val displayName: String
    val id: String
}
