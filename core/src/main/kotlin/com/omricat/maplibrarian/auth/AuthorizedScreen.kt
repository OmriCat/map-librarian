package com.omricat.maplibrarian.auth

import com.omricat.maplibrarian.root.Screen

public data class AuthorizedScreen<ChildRenderingT : Screen>(
    val subScreen: ChildRenderingT,
    val onLogoutClicked: () -> Unit
)
