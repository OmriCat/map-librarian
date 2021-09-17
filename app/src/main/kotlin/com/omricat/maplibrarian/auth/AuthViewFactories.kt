@file:OptIn(WorkflowUiExperimentalApi::class)

package com.omricat.maplibrarian.auth

import com.omricat.maplibrarian.databinding.AttemptingAuthenticationLayoutBinding
import com.omricat.maplibrarian.databinding.LoginLayoutBinding
import com.squareup.workflow1.ui.LayoutRunner
import com.squareup.workflow1.ui.ViewFactory
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.backPressedHandler

internal val AuthLoginViewFactory: ViewFactory<AuthScreen.Login> =
    LayoutRunner.bind(LoginLayoutBinding::inflate) { authScreen, _ ->
        btnLogin.setOnClickListener {
            authScreen.onLoginClicked(
                EmailPasswordCredential(
                    emailAddress = editEmail.text?.toString() ?: "",
                    password = editPassword.text?.toString() ?: ""
                )
            )
        }
        errorMessage.text = authScreen.errorMessage
    }

internal val AuthAttemptingAuthViewFactory: ViewFactory<AuthScreen.AttemptingLogin> =
    LayoutRunner.bind(AttemptingAuthenticationLayoutBinding::inflate) { screen, _ ->
        authenticatingMessageTextView.text = screen.message
        root.backPressedHandler = screen.backPressHandler
    }

internal val AuthViewRegistry = ViewRegistry(
    AuthLoginViewFactory,
    AuthAttemptingAuthViewFactory,
)
