package com.omricat.maplibrarian

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.omricat.maplibrarian.auth.AuthService
import com.omricat.maplibrarian.auth.FirebaseAuthService
import com.omricat.maplibrarian.maplist.FirebaseMapsService
import com.omricat.maplibrarian.maplist.MapsService
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

/**
 * Setup DI container for release variant
 */
@WorkflowUiExperimentalApi
internal fun MapLibraryApp.initializeDI(): MapLibDiContainer = object : DefaultDiContainer() {
    override val authService: AuthService by lazy { FirebaseAuthService(Firebase.auth) }
    override val mapsService: MapsService by lazy { FirebaseMapsService(Firebase.firestore) }
}
