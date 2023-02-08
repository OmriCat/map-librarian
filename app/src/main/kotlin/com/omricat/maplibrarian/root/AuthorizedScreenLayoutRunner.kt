package com.omricat.maplibrarian.root

import android.view.MenuItem
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.omricat.maplibrarian.R
import com.omricat.maplibrarian.auth.AuthorizedScreen
import com.omricat.maplibrarian.chartlist.AddItemDecoratorScreen
import com.omricat.maplibrarian.databinding.AuthorizedScreenBinding
import com.squareup.workflow1.ui.LayoutRunner
import com.squareup.workflow1.ui.LayoutRunner.Companion.bind
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.ViewFactory
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

@OptIn(WorkflowUiExperimentalApi::class)
internal class AuthorizedScreenLayoutRunner(binding: AuthorizedScreenBinding) :
    LayoutRunner<AuthorizedScreen<*>> {

    private val toolbar = binding.toolbar
    private val contentStub = binding.authorizedContentStub
    private val fab = binding.fabPrimaryAction

    private val logoutMenu =
        toolbar.menu.add("Log out").apply { setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER) }

    override fun showRendering(rendering: AuthorizedScreen<*>, viewEnvironment: ViewEnvironment) {
        logoutMenu.setOnMenuItemClickListener {
            rendering.onLogoutClicked()
            true
        }

        val childRendering: Screen =
            rendering.childRendering.let { child ->
                when (child) {
                    is AddItemDecoratorScreen<*> -> {
                        fab.setAsAdd(child.onAddItemClicked)
                        child.childScreen
                    }
                    else -> {
                        fab.reset()
                        child
                    }
                }
            }
        contentStub.update(childRendering, viewEnvironment)
    }

    private fun FloatingActionButton.reset() {
        setImageDrawable(null)
        setOnClickListener(null)
        visibility = View.INVISIBLE
    }

    private fun FloatingActionButton.setAsAdd(onAddItemClicked: () -> Unit) {
        setImageResource(R.drawable.ic_add)
        show()
        setOnClickListener { onAddItemClicked() }
    }

    companion object :
        ViewFactory<AuthorizedScreen<*>> by bind(
            AuthorizedScreenBinding::inflate,
            ::AuthorizedScreenLayoutRunner
        )
}
