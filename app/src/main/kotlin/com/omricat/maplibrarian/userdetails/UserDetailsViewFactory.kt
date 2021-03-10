@file:OptIn(WorkflowUiExperimentalApi::class)

package com.omricat.maplibrarian.userdetails

import com.omricat.maplibrarian.databinding.LayoutUserDetailsBinding
import com.squareup.workflow1.ui.LayoutRunner
import com.squareup.workflow1.ui.ViewFactory
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

internal val UserDetailsViewFactory: ViewFactory<UserDetailsScreen> =
    LayoutRunner.bind(LayoutUserDetailsBinding::inflate) { userDetailsScreen, _ ->
        userId.text = userDetailsScreen.user.username
        this.btnSignOut.setOnClickListener { userDetailsScreen.onLogOutClicked() }
    }
