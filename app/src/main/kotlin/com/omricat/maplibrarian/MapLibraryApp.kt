package com.omricat.maplibrarian

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.omricat.maplibrarian.di.DiContainer
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

@OptIn(WorkflowUiExperimentalApi::class)
@Suppress("unused")
class MapLibraryApp : Application() {
    internal lateinit var diContainer: DiContainer
        private set

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        diContainer = initializeDI()

        // Any initialization that varies for different build variants
        initializeMapLibApp()
    }
}

val Context.diContainer: DiContainer
    get() = (this.applicationContext as MapLibraryApp).diContainer
