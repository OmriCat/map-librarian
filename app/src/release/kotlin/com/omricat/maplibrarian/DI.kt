package com.omricat.maplibrarian

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.omricat.maplibrarian.auth.AuthService
import com.omricat.maplibrarian.chartlist.ChartsService
import com.omricat.maplibrarian.chartlist.FirebaseChartsService
import com.omricat.maplibrarian.firebase.auth.FirebaseAuthService
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

/** Setup DI container for release variant */
@WorkflowUiExperimentalApi
internal fun MapLibraryApp.initializeDI(): DiContainer =
    object : DefaultDiContainer() {
        override val authService: AuthService by lazy { FirebaseAuthService(Firebase.auth) }
        override val chartsService: ChartsService by lazy {
            FirebaseChartsService(Firebase.firestore)
        }
    }
