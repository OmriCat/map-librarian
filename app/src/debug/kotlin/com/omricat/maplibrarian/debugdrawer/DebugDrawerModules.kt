package com.omricat.maplibrarian.debugdrawer

import android.content.Context
import com.jakewharton.processphoenix.ProcessPhoenix
import com.omricat.maplibrarian.BuildConfig
import com.omricat.maplibrarian.R.string
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

internal object DebugDrawerModules {
    private fun emulatorConnectionSettingsModules(
        initialHost: String,
        onValueChanged: (String) -> Unit
    ) =
        arrayOf(
            TextModule("Emulator connection", type = SECTION_HEADER),
            TextInputModule(
                text = { host -> "Host: $host".toText() },
                initialValue = initialHost,
                areRealTimeUpdatesEnabled = false,
                id = "EMULATOR_HOST",
                onValueChanged = onValueChanged
            )
        )

    fun modules(context: Context, debugPreferences: DebugPreferencesRepository): Array<Module<*>> =
        arrayOf(
            HeaderModule(
                string.app_name,
            ),
            KeyValueListModule(
                title = "Build config",
                pairs =
                    listOf(
                        "application id" to BuildConfig.APPLICATION_ID,
                        "version name" to BuildConfig.VERSION_NAME,
                        "version code" to BuildConfig.VERSION_CODE.toString(),
                    )
            ),
            DividerModule(),
            KeylineOverlaySwitchModule(),
            DividerModule(),
        ) +
            emulatorConnectionSettingsModules(
                onValueChanged = { host ->
                    runBlocking { debugPreferences.emulatorHost.edit { host } }
                    Timber.i("Restarting app to reflect updated emulator connection settings")
                    ProcessPhoenix.triggerRebirth(context)
                },
                initialHost =
                    runBlocking { debugPreferences.emulatorHost.value() } ?: "(No host set)"
            )
}
