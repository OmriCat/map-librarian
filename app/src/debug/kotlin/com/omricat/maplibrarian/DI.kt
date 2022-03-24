package com.omricat.maplibrarian

import android.content.Context
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
import kotlinx.coroutines.runBlocking

// Connects to host computer from Android emulator
private const val FIREBASE_EMULATOR_HOST = "10.0.2.2"

private const val FIREBASE_EMULATOR_AUTH_PORT = 9099

private const val FIREBASE_EMULATOR_FIRESTORE_PORT = 8080

@WorkflowUiExperimentalApi
internal fun MapLibraryApp.initializeDI(context: Context): DiContainer =
    object : DefaultDiContainer() {

        private val firebaseEmulatorHost: String by lazy {
            runBlocking {
                DebugPreferencesRepository(context).emulatorHost.value() ?: FIREBASE_EMULATOR_HOST
            }
        }

        override val authService: AuthService by lazy {
            FirebaseAuth.getInstance()
                .useEmulator(firebaseEmulatorHost, FIREBASE_EMULATOR_AUTH_PORT)
            FirebaseAuthService(Firebase.auth)
        }
        override val chartsService: ChartsService by lazy {
            FirebaseFirestore.getInstance().useEmulator(
                firebaseEmulatorHost,
                FIREBASE_EMULATOR_FIRESTORE_PORT
            )
            FirebaseChartsService(Firebase.firestore)
        }
    }
