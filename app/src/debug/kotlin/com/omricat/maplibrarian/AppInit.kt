package com.omricat.maplibrarian

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.omricat.maplibrarian.R.string
import com.pandulapeter.beagle.Beagle
import com.pandulapeter.beagle.common.configuration.Appearance
import com.pandulapeter.beagle.common.configuration.Behavior
import com.pandulapeter.beagle.modules.DividerModule
import com.pandulapeter.beagle.modules.HeaderModule
import com.pandulapeter.beagle.modules.KeyValueListModule
import com.pandulapeter.beagle.modules.KeylineOverlaySwitchModule
import timber.log.Timber
import timber.log.Timber.DebugTree

/**
 * Initialization that should be done for debug build variants
 */
internal fun MapLibraryApp.initializeMapLibApp() {
    Timber.plant(DebugTree())
    Beagle.initialize(
        application = this,
        // Disable shake detection for debug drawer
        behavior = Behavior(
            shakeDetectionBehavior = Behavior.ShakeDetectionBehavior(threshold = null)
        ),
        appearance = Appearance()
    )
    Beagle.set(
        HeaderModule(
            string.app_name,
        ),
        KeyValueListModule(
            title = "Build config",
            pairs = listOf(
                "application id" to BuildConfig.APPLICATION_ID,
                "version name" to BuildConfig.VERSION_NAME,
                "version code" to BuildConfig.VERSION_CODE.toString()
            )
        ),
        DividerModule(),
        KeyValueListModule(
            "Firebase connection",
            pairs = listOf(
                "Firestore IP" to Firebase.firestore.firestoreSettings.host
            )
        ),
        DividerModule(),
        KeylineOverlaySwitchModule(),

        )
}
