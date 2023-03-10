package com.omricat.maplibrarian.firebase

import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Builder

object TestFixtures {
    val app: FirebaseApp by lazy {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
            ?: error("Failed to initialize FirebaseApp")
    }

    val projectId: String
        get() = app.options.projectId ?: error("Can't get projectId from FirebaseApp options")

    fun emulatorBaseUrl(port: Int): HttpUrl =
        Builder().host(FirebaseEmulatorConnection.HOST).port(port).scheme("http").build()
}
