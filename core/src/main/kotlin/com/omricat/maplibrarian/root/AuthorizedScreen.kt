package com.omricat.maplibrarian.root

public data class AuthorizedScreen<ChildRenderingT : Screen>(
    val onLogoutClicked: () -> Unit,
    val subScreen: ChildRenderingT
)
