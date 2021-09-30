package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.omricat.maplibrarian.auth.SignUpScreen.EmailAndPasswordSignUpScreen
import com.omricat.maplibrarian.auth.SignUpScreen.Step
import com.omricat.maplibrarian.auth.SignUpScreen.Step.EnteringEmailAndPassword
import com.omricat.maplibrarian.model.User
import com.omricat.workflow.eventHandler
import com.omricat.workflow.resultWorker
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker

public interface SignUpWorkflow : Workflow<Unit, User?, SignUpScreen> {
    public companion object {
        public fun instance(authService: AuthService): SignUpWorkflow =
            ActualSignUpWorkflow(authService)
    }
}

internal class ActualSignUpWorkflow(private val authService: AuthService) :
    StatefulWorkflow<Unit, State, User?, SignUpScreen>(),
    SignUpWorkflow {
    override fun initialState(props: Unit, snapshot: Snapshot?): State = SignUpPrompt()

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): SignUpScreen = when (renderState) {
        is SignUpPrompt -> EmailAndPasswordSignUpScreen(
            credential = renderState.credential,
            step = EnteringEmailAndPassword(
                errorMessage = renderState.errorMessage,
                onSignUpClicked = context.eventHandler(::onSignUpClicked)
            )
        )

        is AttemptingUserCreation -> {
            context.runningWorker(attemptUserCreation(renderState.credential)) {
                handleUserCreationResult(it, renderState.credential)
            }
            EmailAndPasswordSignUpScreen(
                credential = renderState.credential,
                step = Step.AttemptingUserCreation
            )
        }
    }

    private fun handleUserCreationResult(
        result: Result<User, AuthError>,
        credential: EmailPasswordCredential
    ) = result
        .map { onUserCreated(it) }
        .getOrElse { e -> onErrorCreatingUser(credential, e) }

    private fun onErrorCreatingUser(
        credential: EmailPasswordCredential,
        e: AuthError
    ) = action {
        state = SignUpPrompt(credential = credential, errorMessage = e.message)
    }

    internal fun onUserCreated(user: User) = action { setOutput(user) }

    internal fun onSignUpClicked(credential: EmailPasswordCredential) = action {
        state = AttemptingUserCreation(credential)
    }

    internal fun attemptUserCreation(credential: EmailPasswordCredential): Worker<Result<User, AuthError>> =
        resultWorker(::AuthError) { TODO() }

    override fun snapshotState(state: State): Snapshot? = null
}

internal data class SignUpPrompt(
    val credential: EmailPasswordCredential =
        EmailPasswordCredential("", ""),
    val errorMessage: String = "",
) : State

internal data class AttemptingUserCreation(
    val credential: EmailPasswordCredential
) : State

public interface SignUpScreen : AuthorizingScreen {
    public data class EmailAndPasswordSignUpScreen(
        val credential: EmailPasswordCredential,
        val step: Step
    ) : SignUpScreen

    public sealed interface Step {
        public data class EnteringEmailAndPassword(
            val onSignUpClicked: (EmailPasswordCredential) -> Unit,
            val errorMessage: String = ""
        ) : Step

        public object AttemptingUserCreation : Step
    }
}

internal sealed interface State
