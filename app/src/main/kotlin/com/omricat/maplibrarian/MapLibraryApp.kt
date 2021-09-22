package com.omricat.maplibrarian

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.omricat.maplibrarian.auth.AuthService
import com.omricat.maplibrarian.auth.AuthWorkflow
import com.omricat.maplibrarian.maplist.MapListService
import com.omricat.maplibrarian.maplist.MapsWorkflow

@Suppress("unused")
class MapLibraryApp : Application(), MapLibDiContainer {

    private lateinit var di: MapLibDiContainer

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        di = initializeDI()
        initializeMapLibApp() // Any initialization that varies for different build variants
    }

    override val authService: AuthService
        get() = di.authService
    override val mapListService: MapListService
        get() = di.mapListService

    override val workflows: MapLibDiContainer.Workflows
        get() = di.workflows
}

interface MapLibDiContainer {
    val authService: AuthService
    val mapListService: MapListService
    val workflows: Workflows

    interface Workflows {
        val auth: AuthWorkflow
        val maps: MapsWorkflow
    }
}

val Context.mapLibDiContainer: MapLibDiContainer get() = (this.applicationContext as MapLibDiContainer)
