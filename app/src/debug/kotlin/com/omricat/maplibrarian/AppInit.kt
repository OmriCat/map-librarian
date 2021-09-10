package com.omricat.maplibrarian

import timber.log.Timber
import timber.log.Timber.DebugTree

/**
 * Initialization that should be done for debug build variants
 */
internal fun MapLibraryApp.initializeMapLibApp() {
    Timber.plant(DebugTree())
}
