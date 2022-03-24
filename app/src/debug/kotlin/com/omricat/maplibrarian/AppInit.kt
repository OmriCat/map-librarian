package com.omricat.maplibrarian

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jakewharton.processphoenix.ProcessPhoenix
import com.omricat.maplibrarian.R.string
import com.omricat.maplibrarian.preferences.AppPreference
import com.pandulapeter.beagle.Beagle
import com.pandulapeter.beagle.common.configuration.Appearance
import com.pandulapeter.beagle.common.configuration.Behavior
import com.pandulapeter.beagle.common.configuration.toText
import com.pandulapeter.beagle.common.contracts.module.Module
import com.pandulapeter.beagle.modules.DividerModule
import com.pandulapeter.beagle.modules.HeaderModule
import com.pandulapeter.beagle.modules.KeyValueListModule
import com.pandulapeter.beagle.modules.KeylineOverlaySwitchModule
import com.pandulapeter.beagle.modules.TextInputModule
import com.pandulapeter.beagle.modules.TextModule
import com.pandulapeter.beagle.modules.TextModule.Type.SECTION_HEADER
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import timber.log.Timber.DebugTree

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "debug-settings")

/**
 * Initialization that should be done for debug build variants
 */
internal fun MapLibraryApp.initializeMapLibApp(context: Context) {
    Timber.plant(DebugTree())

    // Enable coroutines debug mode

    System.setProperty(
        kotlinx.coroutines.DEBUG_PROPERTY_NAME, kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
    )
    Beagle.initialize(
        application = this,
        // Disable shake detection for debug drawer
        behavior = Behavior(
            shakeDetectionBehavior = Behavior.ShakeDetectionBehavior(threshold = null)
        ),
        appearance = Appearance()
    )
    val debugPreferences = DebugPreferencesRepository(context)
    Beagle.set(modules = beagleModules(
        onHostChanged = { host ->
            runBlocking { debugPreferences.emulatorHost.edit { host } }
            Timber.i("Restarting app to reflect updated emulator connection settings")
            ProcessPhoenix.triggerRebirth(context)
        },
        initialHost = runBlocking { debugPreferences.emulatorHost.value() }
            ?: "(No host set)"
    ))
}

internal class DebugPreferencesRepository(context: Context) {

    val emulatorHost = AppPreference(context.dataStore, stringPreferencesKey("emulator-host"))
}

private fun emulatorConnectionSettingsModules(
    initialHost: String,
    onValueChanged: (String) -> Unit
) = arrayOf(
    TextModule("Emulator connection", type = SECTION_HEADER),
    TextInputModule(
        text = { host -> "Host: $host".toText() },
        initialValue = initialHost,
        areRealTimeUpdatesEnabled = false,
        id = "EMULATOR_HOST",
        onValueChanged = onValueChanged
    )
)

private fun beagleModules(
    initialHost: String,
    onHostChanged: (String) -> Unit
): Array<Module<*>> =
    arrayOf(
        HeaderModule(
            string.app_name,
        ),
        KeyValueListModule(
            title = "Build config",
            pairs = listOf(
                "application id" to BuildConfig.APPLICATION_ID,
                "version name" to BuildConfig.VERSION_NAME,
                "version code" to BuildConfig.VERSION_CODE.toString(),
            )
        ),
        DividerModule(),
        KeylineOverlaySwitchModule(),
        DividerModule(),
    ) + emulatorConnectionSettingsModules(initialHost, onHostChanged)
