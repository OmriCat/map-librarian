@file:OptIn(WorkflowUiExperimentalApi::class)

package com.omricat.maplibrarian.maplist

import com.omricat.maplibrarian.databinding.ErrorBinding
import com.omricat.maplibrarian.databinding.MaplistLoadingBinding
import com.squareup.workflow1.ui.LayoutRunner.Companion.bind
import com.squareup.workflow1.ui.ViewFactory
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

internal val MapListLoadingViewFactory: ViewFactory<MapsScreen.Loading> =
    bind(MaplistLoadingBinding::inflate) { _, _ -> }

internal val MapsErrorViewFactory: ViewFactory<MapsScreen.ShowError> =
    bind(ErrorBinding::inflate) { error, _ ->
        errorMessage.text = error.message
    }

@WorkflowUiExperimentalApi
internal val MapListViewRegistry = ViewRegistry(
    MapListLoadingViewFactory,
    MapsErrorViewFactory,
    MapListLayoutRunner,
    AddingItemScreenViewFactory,
)
