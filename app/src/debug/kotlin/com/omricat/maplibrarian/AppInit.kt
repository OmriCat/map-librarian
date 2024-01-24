package com.omricat.maplibrarian

import com.omricat.maplibrarian.debugdrawer.DebugDrawerModules
import com.omricat.maplibrarian.debugdrawer.DebugPreferencesRepository
import com.pandulapeter.beagle.Beagle
import com.pandulapeter.beagle.common.configuration.Appearance
import com.pandulapeter.beagle.common.configuration.Behavior

/** Initialization that should be done for debug build variants */
internal fun MapLibraryApp.initializeMapLibApp() {

    // Enable coroutines debug mode
    System.setProperty(
        kotlinx.coroutines.DEBUG_PROPERTY_NAME, kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
    )

    Beagle.initialize(
        application = this,
        // Disable shake detection for debug drawer
        behavior = Behavior(shakeDetectionBehavior = Behavior.ShakeDetectionBehavior(threshold = null)),
        appearance = Appearance()
    )
    Beagle.set(
        modules = DebugDrawerModules.modules(
            this, DebugPreferencesRepository(this), diContainer.logger
        )
    )
}
