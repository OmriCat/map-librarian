@file:OptIn(
    WorkflowUiExperimentalApi::class
)

package com.omricat.maplibrarian.root

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omricat.maplibrarian.auth.AuthViewRegistry
import com.omricat.maplibrarian.userdetails.UserDetailsViewFactory
import com.squareup.workflow1.SimpleLoggingWorkflowInterceptor
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowLayout
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.plus
import com.squareup.workflow1.ui.renderWorkflowIn
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.ExperimentalTime

@ExperimentalTime
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val model: MainViewModel by viewModels()

        setContentView(WorkflowLayout(this).apply { start(model.renderings, viewRegistry) })
    }

    private companion object {
        val viewRegistry: ViewRegistry = AuthViewRegistry + (ViewRegistry(UserDetailsViewFactory))
    }
}

@ExperimentalTime
class MainViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    val renderings: StateFlow<MainScreen> by lazy {
        renderWorkflowIn(
            workflow = MainWorkflow,
            scope = viewModelScope,
            savedStateHandle = savedState,
            interceptors = listOf(SimpleLoggingWorkflowInterceptor())
        )
    }
}
