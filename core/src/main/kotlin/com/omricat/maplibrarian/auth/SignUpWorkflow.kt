package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.omricat.maplibrarian.auth.SignUpOutput.SignUpCancelled
import com.omricat.maplibrarian.auth.SignUpOutput.UserCreated
import com.omricat.maplibrarian.auth.SignUpScreen.EmailAndPasswordSignUpScreen
import com.omricat.maplibrarian.auth.SignUpScreen.Step
import com.omricat.maplibrarian.auth.SignUpScreen.Step.EnteringEmailAndPassword
import com.omricat.maplibrarian.auth.State.AttemptingUserCreation
import com.omricat.maplibrarian.auth.State.SignUpPrompt
import com.omricat.maplibrarian.model.User
import com.omricat.workflow.eventHandler
import com.omricat.workflow.resultWorker
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker

public interface SignUpWorkflow : Workflow<Unit, SignUpOutput, SignUpScreen> {
    public companion object {
        public fun instance(userRepository: UserRepository): SignUpWorkflow =
            ActualSignUpWorkflow(userRepository)
    }
}

internal class ActualSignUpWorkflow(private val userRepository: UserRepository) :
    StatefulWorkflow<Unit, State, SignUpOutput, SignUpScreen>(), SignUpWorkflow {
    override fun initialState(props: Unit, snapshot: Snapshot?): State = SignUpPrompt()

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): SignUpScreen =
        when (renderState) {
            is SignUpPrompt ->
                EmailAndPasswordSignUpScreen(
                    credential = renderState.credential,
                    step =
                        EnteringEmailAndPassword(
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
        result: Result<User, UserRepository.Error>,
        credential: EmailPasswordCredential
    ) = result.map { onUserCreated(it) }.getOrElse { e -> onErrorCreatingUser(credential, e) }

    internal fun onErrorCreatingUser(credential: EmailPasswordCredential, e: UserRepository.Error) =
        action {
            this.state = SignUpPrompt(credential = credential, errorMessage = e.message)
        }

    internal fun onUserCreated(user: User) = action { setOutput(UserCreated(user)) }

    internal fun onSignUpClicked(credential: EmailPasswordCredential) = action {
        state = AttemptingUserCreation(credential)
    }

    internal fun onSignUpCancelled() = action { setOutput(SignUpCancelled) }

    internal fun attemptUserCreation(
        credential: EmailPasswordCredential
    ): Worker<Result<User, UserRepository.Error>> =
        resultWorker(::ExceptionWrapperError) { userRepository.createUser(credential) }
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
        val credential: EmailPasswordCredential = EmailPasswordCredential("", ""),
        val errorMessage: String = "",
    ) : State

    data class AttemptingUserCreation(val credential: EmailPasswordCredential) : State
}
