@file:OptIn(WorkflowUiExperimentalApi::class)

package com.omricat.maplibrarian.chartlist

import com.omricat.maplibrarian.databinding.ChartlistLoadingBinding
import com.omricat.maplibrarian.databinding.ErrorBinding
import com.squareup.workflow1.ui.LayoutRunner.Companion.bind
import com.squareup.workflow1.ui.ViewFactory
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

internal val ChartListLoadingViewFactory: ViewFactory<ChartsScreen.Loading> =
    bind(ChartlistLoadingBinding::inflate) { _, _ -> }

internal val ChartErrorViewFactory: ViewFactory<ChartsScreen.ShowError> =
    bind(ErrorBinding::inflate) { error, _ ->
        errorMessage.text = error.message
    }

@WorkflowUiExperimentalApi
internal val MapListViewRegistry = ViewRegistry(
    ChartListLoadingViewFactory,
    ChartErrorViewFactory,
    ChartListLayoutRunner,
    EditChartDetailsScreenViewFactory,
    SavingItemScreenViewFactory
)
