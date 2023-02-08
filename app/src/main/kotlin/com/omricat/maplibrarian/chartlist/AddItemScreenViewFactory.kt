package com.omricat.maplibrarian.chartlist

import android.view.View
import com.omricat.maplibrarian.databinding.EditChartBinding
import com.squareup.workflow1.ui.LayoutRunner.Companion.bind
import com.squareup.workflow1.ui.ViewFactory
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.setTextChangedListener
import com.squareup.workflow1.ui.updateText

@OptIn(WorkflowUiExperimentalApi::class)
internal object AddItemScreenViewFactory :
    ViewFactory<AddItemScreen> by bind(
        bindingInflater = EditChartBinding::inflate,
        showRendering = { screen, _ ->
            editTitle.updateText(screen.chart.title)
            savingError.text = screen.errorMessage
            enableSaveAndDiscard(screen)
        }
    )

@OptIn(WorkflowUiExperimentalApi::class)
internal object SavingItemScreenViewFactory :
    ViewFactory<SavingItemScreen> by bind(
        bindingInflater = EditChartBinding::inflate,
        showRendering = { screen, _ ->
            editTitle.updateText(screen.chart.title)
            enableSaveAndDiscard(null)
        }
    )

@WorkflowUiExperimentalApi
private fun EditChartBinding.enableSaveAndDiscard(screen: AddItemScreen?) {
    fun (() -> Unit).asClickListener(): (View) -> Unit = { _ -> this.invoke() }

    val enabled = screen != null
    btnDiscard.setOnClickListener(screen?.discardChanges?.asClickListener())
    btnDiscard.isEnabled = enabled
    btnSave.setOnClickListener(screen?.saveChanges?.asClickListener())
    btnSave.isEnabled = enabled
    editTitle.setTextChangedListener(screen?.onTitleChanged)
    editTitle.isEnabled = enabled
}
