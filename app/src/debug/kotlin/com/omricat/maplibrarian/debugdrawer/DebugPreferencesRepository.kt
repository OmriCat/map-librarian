package com.omricat.maplibrarian.debugdrawer

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.omricat.maplibrarian.preferences.AppPreference

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "debug-settings")

internal class DebugPreferencesRepository(context: Context) {

    val emulatorHost = AppPreference(context.dataStore, stringPreferencesKey("emulator-host"))
}
