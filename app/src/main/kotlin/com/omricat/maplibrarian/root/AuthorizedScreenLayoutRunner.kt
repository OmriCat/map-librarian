package com.omricat.maplibrarian.root

import android.view.MenuItem
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

    private val logoutMenu = toolbar.menu.add("Log out").apply {
        setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
    }

    override fun showRendering(rendering: AuthorizedScreen<*>, viewEnvironment: ViewEnvironment) {
        logoutMenu.setOnMenuItemClickListener { rendering.onLogoutClicked(); true }
        contentStub.update(rendering.subScreen, viewEnvironment)
    }

    companion object : ViewFactory<AuthorizedScreen<*>> by bind(
        AuthorizedScreenBinding::inflate,
        ::AuthorizedScreenLayoutRunner
    )
}
