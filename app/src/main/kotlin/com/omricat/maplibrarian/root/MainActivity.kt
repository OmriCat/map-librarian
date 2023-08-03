@file:OptIn(WorkflowUiExperimentalApi::class)

package com.omricat.maplibrarian.root

import android.app.Application
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Tag
import com.omricat.logging.Logger
import com.omricat.maplibrarian.diContainer
import com.squareup.workflow1.SimpleLoggingWorkflowInterceptor
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowLayout
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.renderWorkflowIn
import kotlinx.coroutines.flow.StateFlow

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val model: MainViewModel by viewModels()
        val viewRegistry: ViewRegistry = diContainer.viewRegistry

        setContentView(WorkflowLayout(this).apply { start(model.renderings, viewRegistry) })
    }
}

internal class MainViewModel(app: Application, private val savedState: SavedStateHandle) :
    AndroidViewModel(app) {
    private val diContainer = app.diContainer
    val renderings: StateFlow<Screen> by lazy {
        renderWorkflowIn(
            workflow = diContainer.workflows.root,
            scope = viewModelScope,
            savedStateHandle = savedState,
            interceptors = listOf(MapLibLoggerLoggingWorkflowInterceptor(diContainer.logger))
        )
    }
}

internal class MapLibLoggerLoggingWorkflowInterceptor(private val logger: Logger) :
    SimpleLoggingWorkflowInterceptor() {
    override fun log(text: String) {
        logger.log(tag = Tag("WorkflowLogging")) { text }
    }
}
