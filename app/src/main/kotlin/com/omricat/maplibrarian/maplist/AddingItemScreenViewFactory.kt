package com.omricat.maplibrarian.maplist

import com.omricat.maplibrarian.databinding.EditMapBinding
import com.squareup.workflow1.ui.LayoutRunner.Companion.bind
import com.squareup.workflow1.ui.ViewFactory
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.setTextChangedListener
import com.squareup.workflow1.ui.updateText

@OptIn(WorkflowUiExperimentalApi::class)
internal object AddingItemScreenViewFactory : ViewFactory<AddingItemScreen> by bind(
    bindingInflater = EditMapBinding::inflate,
    showRendering = { screen, _ ->
        editTitle.updateText(screen.map.title)
        val enabled = when (screen) {
            is AddItemScreen -> {
                btnDiscard.setOnClickListener { screen.discardChanges() }
                btnSave.setOnClickListener { screen.saveChanges() }
                editTitle.setTextChangedListener(screen.onTitleChanged)
                true
            }
            is SavingItemScreen -> false
        }
        editTitle.isEnabled = enabled
        btnDiscard.isEnabled = enabled
        btnSave.isEnabled = enabled
    }
)
