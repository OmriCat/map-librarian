package com.omricat.maplibrarian.auth

import com.omricat.maplibrarian.auth.ActualAuthWorkflow.State
import com.omricat.maplibrarian.auth.AuthResult.Authenticated
import com.omricat.maplibrarian.model.UserUid
import com.squareup.workflow1.StatelessWorkflow
import com.squareup.workflow1.applyTo
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf

public class ActualAuthWorkflowTest :
    StringSpec({
        "onAuthError action returns to login prompt" {
            val workflow = ActualAuthWorkflow(TestAuthService(), NullSignupWorkflow)
            val fakeCredential = EmailPasswordCredential("a@b.com", "12345")
            val (newState, maybeOutput) =
                workflow
                    .onAuthError(AuthError("Authentication failure"))
                    .applyTo(props = Unit, state = State.AttemptingAuthorization(fakeCredential))

            assertSoftly {
                maybeOutput.shouldBeNull()

                newState.shouldBeTypeOf<State.LoginPrompt>()
                newState.errorMessage.shouldBe("Authentication failure")
            }
        }

        "onNoAuthenticatedUser action transitions to LoginPrompt" {
            val workflow = ActualAuthWorkflow(TestAuthService(), NullSignupWorkflow)
            val (newState, maybeOutput) =
                workflow.onNoAuthenticatedUser.applyTo(
                    props = Unit,
                    state = State.PossibleLoggedInUser
                )

            assertSoftly {
                maybeOutput.shouldBeNull()
                newState.shouldBeTypeOf<State.LoginPrompt>()
            }
        }

        "onAuthenticated action outputs Authenticated(user) from workflow" {
            val workflow = ActualAuthWorkflow(TestAuthService(), NullSignupWorkflow)
            val fakeCredential = EmailPasswordCredential("a@b.com", "12345")
            val fakeUser = TestUser("user1", UserUid("1"))
            val (_, maybeOutput) =
                workflow
                    .onAuthenticated(fakeUser)
                    .applyTo(props = Unit, state = State.AttemptingAuthorization(fakeCredential))

            assertSoftly {
                maybeOutput.shouldNotBeNull()
                maybeOutput.value.let { result ->
                    result.shouldBeTypeOf<Authenticated>()
                    result.user.shouldBe(fakeUser)
                }
            }
        }
    })

private object NullSignupWorkflow :
    SignUpWorkflow, StatelessWorkflow<Unit, SignUpOutput, SignUpScreen>() {
    override fun render(renderProps: Unit, context: RenderContext): SignUpScreen =
        object : SignUpScreen {}
}
