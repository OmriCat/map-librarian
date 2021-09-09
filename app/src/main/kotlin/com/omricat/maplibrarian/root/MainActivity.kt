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
import com.omricat.maplibrarian.auth.AuthViewRegistry
import com.omricat.maplibrarian.mapLibDiContainer
import com.omricat.maplibrarian.maplist.MapListViewRegistry
import com.squareup.workflow1.SimpleLoggingWorkflowInterceptor
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowLayout
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.plus
import com.squareup.workflow1.ui.renderWorkflowIn
import kotlinx.coroutines.flow.StateFlow

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val model: MainViewModel by viewModels()

        setContentView(WorkflowLayout(this).apply { start(model.renderings, viewRegistry) })
    }

    private companion object {
        val viewRegistry: ViewRegistry = AuthViewRegistry + MapListViewRegistry
    }
}

class MainViewModel(app: Application, private val savedState: SavedStateHandle) :
    AndroidViewModel(app) {
    private val diContainer = app.mapLibDiContainer
    val renderings: StateFlow<MainScreen> by lazy {
        renderWorkflowIn(
            workflow = MainWorkflow(
                diContainer.authService,
                diContainer.workflows.auth,
                diContainer.workflows.mapList
            ),
            scope = viewModelScope,
            savedStateHandle = savedState,
            interceptors = listOf(SimpleLoggingWorkflowInterceptor())
        )
    }
}
