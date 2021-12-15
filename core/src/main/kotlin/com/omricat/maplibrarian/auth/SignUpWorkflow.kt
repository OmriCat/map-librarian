package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.omricat.maplibrarian.auth.ActualSignUpWorkflow.Actions.OnErrorCreatingUser
import com.omricat.maplibrarian.auth.ActualSignUpWorkflow.Actions.OnSignUpCancelled
import com.omricat.maplibrarian.auth.ActualSignUpWorkflow.Actions.OnSignUpClicked
import com.omricat.maplibrarian.auth.ActualSignUpWorkflow.Actions.OnUserCreated
import com.omricat.maplibrarian.auth.SignUpOutput.SignUpCancelled
import com.omricat.maplibrarian.auth.SignUpOutput.UserCreated
import com.omricat.maplibrarian.auth.SignUpScreen.EmailAndPasswordSignUpScreen
import com.omricat.maplibrarian.auth.SignUpScreen.Step
import com.omricat.maplibrarian.auth.SignUpScreen.Step.EnteringEmailAndPassword
import com.omricat.maplibrarian.auth.State.AttemptingUserCreation
import com.omricat.maplibrarian.auth.State.SignUpPrompt
import com.omricat.maplibrarian.model.User
import com.omricat.workflow.AbstractWorkflowAction
import com.omricat.workflow.eventHandler
import com.omricat.workflow.resultWorker
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.WorkflowAction
import com.squareup.workflow1.runningWorker

public interface SignUpWorkflow : Workflow<Unit, SignUpOutput, SignUpScreen> {
    public companion object {
        public fun instance(authService: AuthService): SignUpWorkflow =
            ActualSignUpWorkflow(authService)
    }
}

internal class ActualSignUpWorkflow(private val authService: AuthService) :
    StatefulWorkflow<Unit, State, SignUpOutput, SignUpScreen>(),
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
                onSignUpClicked = context.eventHandler(::onSignUpClicked),
            ),
            backPressHandler = context.eventHandler(::onSignUpCancelled)
        )

        is AttemptingUserCreation -> {
            context.runningWorker(attemptUserCreation(renderState.credential)) {
                handleUserCreationResult(it, renderState.credential)
            }
            EmailAndPasswordSignUpScreen(
                credential = renderState.credential,
                backPressHandler = context.eventHandler(::onSignUpCancelled),
                step = Step.CreatingUser
            )
        }
    }

    override fun snapshotState(state: State): Snapshot? = null

    private fun handleUserCreationResult(
        result: Result<User, AuthError>,
        credential: EmailPasswordCredential
    ) = result
        .map { onUserCreated(it) }
        .getOrElse { e -> onErrorCreatingUser(credential, e) }

    internal fun onErrorCreatingUser(
        credential: EmailPasswordCredential,
        e: AuthError
    ) = OnErrorCreatingUser(credential, e)

    internal fun onUserCreated(user: User) = OnUserCreated(user)

    internal fun onSignUpClicked(credential: EmailPasswordCredential) = OnSignUpClicked(credential)

    internal fun onSignUpCancelled() = OnSignUpCancelled

    internal object Actions {

        class OnErrorCreatingUser(credential: EmailPasswordCredential, e: AuthError) : Action(
            { state = SignUpPrompt(credential = credential, errorMessage = e.message) },
            "$this"
        )

        class OnSignUpClicked(credential: EmailPasswordCredential) :
            Action({ state = AttemptingUserCreation(credential) }, "$this")

        class OnUserCreated(user: User) : Action({ setOutput(UserCreated(user)) }, "$this")

        object OnSignUpCancelled : Action({ setOutput(SignUpCancelled) }, "$this")
    }

    internal open class Action(
        updater: WorkflowAction<Unit, State, SignUpOutput>.Updater.() -> Unit,
        name: String
    ) : AbstractWorkflowAction<Unit, State, SignUpOutput>({ name }, updater)

    internal fun attemptUserCreation(credential: EmailPasswordCredential):
        Worker<Result<User, AuthError>> =
        resultWorker(::AuthError) { authService.createUser(credential) }
}

public sealed interface SignUpOutput {
    public data class UserCreated(val user: User) : SignUpOutput
    public object SignUpCancelled : SignUpOutput
}

public interface SignUpScreen : AuthorizingScreen {
    public data class EmailAndPasswordSignUpScreen(
        val credential: EmailPasswordCredential,
        val backPressHandler: BackPressHandler,
        val step: Step
    ) : SignUpScreen

    public sealed interface Step {
        public data class EnteringEmailAndPassword(
            val onSignUpClicked: (EmailPasswordCredential) -> Unit,
            val errorMessage: String = ""
        ) : Step

        public object CreatingUser : Step
    }
}

internal sealed interface State {
    data class SignUpPrompt(
        val credential: EmailPasswordCredential =
            EmailPasswordCredential("", ""),
        val errorMessage: String = "",
    ) : State

    data class AttemptingUserCreation(
        val credential: EmailPasswordCredential
    ) : State
}
