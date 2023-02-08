package com.omricat.maplibrarian.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

internal class AppPreference(
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<String>
) {
    suspend fun value(): String? = dataStore.data.map { prefs -> prefs[key] }.firstOrNull()

    suspend fun edit(transform: (String?) -> String) =
        dataStore.edit { prefs -> prefs[key] = transform(prefs[key]) }
}
