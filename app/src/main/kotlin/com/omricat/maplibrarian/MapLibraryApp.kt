@file:OptIn(WorkflowUiExperimentalApi::class)

package com.omricat.maplibrarian

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.omricat.maplibrarian.auth.AuthService
import com.omricat.maplibrarian.auth.AuthWorkflow
import com.omricat.maplibrarian.chartlist.ChartsService
import com.omricat.maplibrarian.chartlist.ChartsWorkflow
import com.omricat.maplibrarian.chartlist.EditChartDetailsWorkflow
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

@Suppress("unused")
class MapLibraryApp : Application() {

    lateinit var diContainer: DiContainer

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        diContainer = initializeDI()
        initializeMapLibApp() // Any initialization that varies for different build variants
    }
}

interface DiContainer {
    val authService: AuthService
    val chartsService: ChartsService
    val workflows: Workflows
    val viewRegistry: ViewRegistry

    interface Workflows {
        val auth: AuthWorkflow
        val charts: ChartsWorkflow
        val editChartDetailsWorkflow: EditChartDetailsWorkflow
    }
}

val Context.diContainer: DiContainer
    get() = (this.applicationContext as MapLibraryApp).diContainer
