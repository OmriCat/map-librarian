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
import com.omricat.maplibrarian.diContainer
import com.squareup.workflow1.SimpleLoggingWorkflowInterceptor
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowLayout
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.renderWorkflowIn
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

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
            workflow = RootWorkflow(
                diContainer.authService,
                diContainer.workflows.auth,
                diContainer.workflows.charts
            ),
            scope = viewModelScope,
            savedStateHandle = savedState,
            interceptors = listOf(TimberLoggingWorkflowInterceptor)
        )
    }
}

internal object TimberLoggingWorkflowInterceptor : SimpleLoggingWorkflowInterceptor() {
    override fun log(text: String) {
        Timber.d(text)
    }
}
