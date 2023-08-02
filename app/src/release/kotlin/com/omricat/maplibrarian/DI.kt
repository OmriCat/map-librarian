package com.omricat.maplibrarian

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.omricat.maplibrarian.auth.FirebaseUserRepository
import com.omricat.maplibrarian.auth.UserRepository
import com.omricat.maplibrarian.chartlist.ChartsRepository
import com.omricat.maplibrarian.chartlist.FirebaseChartsRepository
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

/** Setup DI container for release variant */
@WorkflowUiExperimentalApi
internal fun MapLibraryApp.initializeDI(): DiContainer =
    object : DefaultDiContainer() {
        override val userRepository: UserRepository by lazy {
            FirebaseUserRepository(Firebase.auth)
        }
        override val chartsRepository: ChartsRepository by lazy {
            FirebaseChartsRepository(Firebase.firestore)
        }
    }
