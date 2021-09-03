package com.omricat.maplibrarian

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.omricat.maplibrarian.auth.AuthService
import com.omricat.maplibrarian.auth.AuthWorkflow
import com.omricat.maplibrarian.userdetails.UserDetailsWorkFlow

@Suppress("unused")
class MapLibraryApp : Application(), MapLibDiContainer {

    private lateinit var di: MapLibDiContainer

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        di = initializeDI()
    }

    override val authService: AuthService
        get() = di.authService

    override val workflows: MapLibDiContainer.Workflows
        get() = di.workflows
}

interface MapLibDiContainer {
    val authService: AuthService
    val workflows: Workflows

    interface Workflows {
        val auth: AuthWorkflow
        val userDetails: UserDetailsWorkFlow
    }
}

val Context.mapLibDiContainer: MapLibDiContainer get() = (this.applicationContext as MapLibDiContainer)
