package com.omricat.maplibrarian

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.omricat.maplibrarian.auth.FirebaseUserRepository
import com.omricat.maplibrarian.auth.UserRepository
import com.omricat.maplibrarian.chartlist.ChartsRepository
import com.omricat.maplibrarian.chartlist.FirebaseChartsRepository
import com.omricat.maplibrarian.debugdrawer.DebugPreferencesRepository
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import kotlinx.coroutines.runBlocking

// Connects to host computer from Android emulator
private const val FIREBASE_EMULATOR_HOST = "10.0.2.2"

private const val FIREBASE_EMULATOR_AUTH_PORT = 9099

private const val FIREBASE_EMULATOR_FIRESTORE_PORT = 8080

@WorkflowUiExperimentalApi
internal fun MapLibraryApp.initializeDI(): DiContainer =
    object : DefaultDiContainer() {

        private val firebaseEmulatorHost: String by lazy {
            runBlocking {
                DebugPreferencesRepository(this@initializeDI).emulatorHost.value()
                    ?: FIREBASE_EMULATOR_HOST
            }
        }

        override val userRepository: UserRepository by lazy {
            FirebaseAuth.getInstance()
                .useEmulator(firebaseEmulatorHost, FIREBASE_EMULATOR_AUTH_PORT)
            FirebaseUserRepository(Firebase.auth)
        }
        override val chartsRepository: ChartsRepository by lazy {
            FirebaseFirestore.getInstance()
                .useEmulator(firebaseEmulatorHost, FIREBASE_EMULATOR_FIRESTORE_PORT)
            FirebaseChartsRepository(Firebase.firestore)
        }
    }
