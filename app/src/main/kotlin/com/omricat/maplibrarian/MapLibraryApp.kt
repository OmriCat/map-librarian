@file:OptIn(WorkflowUiExperimentalApi::class)

package com.omricat.maplibrarian

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.omricat.maplibrarian.auth.AuthService
import com.omricat.maplibrarian.auth.AuthWorkflow
import com.omricat.maplibrarian.maplist.MapsService
import com.omricat.maplibrarian.maplist.MapsWorkflow
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

@Suppress("unused")
class MapLibraryApp : Application() {

    lateinit var diContainer: MapLibDiContainer

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        diContainer = initializeDI()
        initializeMapLibApp() // Any initialization that varies for different build variants
    }
}

interface MapLibDiContainer {
    val authService: AuthService
    val mapsService: MapsService
    val workflows: Workflows
    val viewRegistry: ViewRegistry

    interface Workflows {
        val auth: AuthWorkflow
        val maps: MapsWorkflow
    }
}

val Context.mapLibDiContainer: MapLibDiContainer
    get() = (this.applicationContext as MapLibraryApp).diContainer
