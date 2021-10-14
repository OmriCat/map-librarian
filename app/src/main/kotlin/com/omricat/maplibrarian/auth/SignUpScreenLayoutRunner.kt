package com.omricat.maplibrarian.auth

import com.omricat.maplibrarian.databinding.SignUpScreenBinding
import com.squareup.workflow1.ui.LayoutRunner
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.ViewFactory
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.backPressedHandler
import com.squareup.workflow1.ui.updateText

@WorkflowUiExperimentalApi
class SignUpScreenLayoutRunner(private val binding: SignUpScreenBinding) :
    LayoutRunner<SignUpScreen.EmailAndPasswordSignUpScreen> {
    override fun showRendering(
        rendering: SignUpScreen.EmailAndPasswordSignUpScreen,
        viewEnvironment: ViewEnvironment
    ) =
        with(binding) {
            signUpEmail.updateText(rendering.credential.emailAddress)
            signUpPassword.updateText(rendering.credential.password)
            root.backPressedHandler = rendering.backPressHandler
            when (val step = rendering.step) {
                is SignUpScreen.Step.EnteringEmailAndPassword -> {
                    btnSignUp.setOnClickListener { step.onSignUpClicked(
                        EmailPasswordCredential(
                        signUpEmail.text?.toString().orEmpty(),
                            signUpPassword.text?.toString().orEmpty()
                    )
                    ) }
                    btnSignUp.isEnabled = true
                    errorMessage.text = step.errorMessage
                }
                is SignUpScreen.Step.CreatingUser -> {
                    btnSignUp.setOnClickListener(null)
                    btnSignUp.isEnabled = false
                    errorMessage.text = ""
                }
            }
        }

    companion object SignUpScreenViewFactory :
        ViewFactory<SignUpScreen.EmailAndPasswordSignUpScreen> by
        LayoutRunner.bind(SignUpScreenBinding::inflate, ::SignUpScreenLayoutRunner)
}
