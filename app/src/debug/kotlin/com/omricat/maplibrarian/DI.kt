package com.omricat.maplibrarian

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.omricat.maplibrarian.auth.AuthService
import com.omricat.maplibrarian.auth.FirebaseAuthService
import com.omricat.maplibrarian.chartlist.ChartsService
import com.omricat.maplibrarian.chartlist.FirebaseChartsService
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

private const val FIREBASE_EMULATOR_HOST = "192.168.1.17"

private const val FIREBASE_EMULATOR_AUTH_PORT = 9099

private const val FIREBASE_EMULATOR_FIRESTORE_PORT = 8080

@WorkflowUiExperimentalApi
internal fun MapLibraryApp.initializeDI(): DiContainer = object : DefaultDiContainer() {
    override val authService: AuthService by lazy {
        FirebaseAuth.getInstance().useEmulator(FIREBASE_EMULATOR_HOST, FIREBASE_EMULATOR_AUTH_PORT)
        FirebaseAuthService(Firebase.auth)
    }
    override val chartsService: ChartsService by lazy {
        FirebaseFirestore.getInstance().useEmulator(
            FIREBASE_EMULATOR_HOST,
            FIREBASE_EMULATOR_FIRESTORE_PORT
        )
        FirebaseChartsService(Firebase.firestore)
    }
}
