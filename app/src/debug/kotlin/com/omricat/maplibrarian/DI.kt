package com.omricat.maplibrarian

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.omricat.maplibrarian.auth.ActualAuthWorkflow
import com.omricat.maplibrarian.auth.AuthService
import com.omricat.maplibrarian.auth.AuthWorkflow
import com.omricat.maplibrarian.auth.FirebaseAuthService
import com.omricat.maplibrarian.maplist.FirebaseMapListService
import com.omricat.maplibrarian.maplist.MapListService
import com.omricat.maplibrarian.maplist.MapsWorkflow

private const val FIREBASE_EMULATOR_HOST = "192.168.1.17"

private const val FIREBASE_EMULATOR_AUTH_PORT = 9099

private const val FIREBASE_EMULATOR_FIRESTORE_PORT = 8080

internal fun MapLibraryApp.initializeDI(): MapLibDiContainer = object : MapLibDiContainer {
    override val authService: AuthService by lazy {
        FirebaseAuth.getInstance().useEmulator(FIREBASE_EMULATOR_HOST, FIREBASE_EMULATOR_AUTH_PORT)
        FirebaseAuthService(Firebase.auth)
    }
    override val mapListService: MapListService by lazy {
        FirebaseFirestore.getInstance().useEmulator(
            FIREBASE_EMULATOR_HOST,
            FIREBASE_EMULATOR_FIRESTORE_PORT
        )
        FirebaseMapListService(Firebase.firestore)
    }

    override val workflows: MapLibDiContainer.Workflows = object : MapLibDiContainer.Workflows {
        override val auth: AuthWorkflow by lazy { ActualAuthWorkflow(authService) }

        override val maps: MapsWorkflow = MapsWorkflow(mapListService)
    }
}
