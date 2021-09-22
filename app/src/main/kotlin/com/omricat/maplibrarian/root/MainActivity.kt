@file:OptIn(
    WorkflowUiExperimentalApi::class
)

package com.omricat.maplibrarian.root

import android.app.Application
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.omricat.maplibrarian.mapLibDiContainer
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
        val viewRegistry: ViewRegistry = mapLibDiContainer.viewRegistry

        setContentView(WorkflowLayout(this).apply { start(model.renderings, viewRegistry) })
    }
}

internal class MainViewModel(app: Application, private val savedState: SavedStateHandle) :
    AndroidViewModel(app) {
    private val diContainer = app.mapLibDiContainer
    val renderings: StateFlow<Screen> by lazy {
        renderWorkflowIn(
            workflow = RootWorkflow(
                diContainer.authService,
                diContainer.workflows.auth,
                diContainer.workflows.maps
            ),
            scope = viewModelScope,
            savedStateHandle = savedState,
            interceptors = listOf(SimpleLoggingWorkflowInterceptor())
        )
    }
}
