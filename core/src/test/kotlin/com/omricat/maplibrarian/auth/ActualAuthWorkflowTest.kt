package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.omricat.maplibrarian.auth.ActualAuthWorkflow.State
import com.omricat.maplibrarian.auth.AuthResult.Authenticated
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.model.UserUid
import com.squareup.workflow1.applyTo
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf

public class ActualAuthWorkflowTest : StringSpec({
    "onAuthError action returns to login prompt" {
        val workflow = ActualAuthWorkflow(TestAuthService())
        val fakeCredential = EmailPasswordCredential("a@b.com", "12345")
        val (newState, maybeOutput) = workflow.onAuthError(AuthError("Authentication failure"))
            .applyTo(
                props = Unit,
                state = State.AttemptingAuthorization(fakeCredential)
            )

        assertSoftly {
            maybeOutput.shouldBeNull()

            newState.shouldBeTypeOf<State.LoginPrompt>()
            newState.errorMessage.shouldBe("Authentication failure")
        }
    }

    "onNoAuthenticatedUser action transitions to LoginPrompt" {
        val workflow = ActualAuthWorkflow(TestAuthService())
        val (newState, maybeOutput) = workflow.onNoAuthenticatedUser()
            .applyTo(
                props = Unit,
                state = State.PossibleLoggedInUser
            )

        assertSoftly {
            maybeOutput.shouldBeNull()
            newState.shouldBeTypeOf<State.LoginPrompt>()
        }
    }

    "onAuthenticated action outputs Authenticated(user) from workflow" {
        val workflow = ActualAuthWorkflow(TestAuthService())
        val fakeCredential = EmailPasswordCredential("a@b.com", "12345")
        val fakeUser = TestUser("user1", UserUid("1"))
        val (_, maybeOutput) = workflow.onAuthenticated(fakeUser)
            .applyTo(
                props = Unit,
                state = State.AttemptingAuthorization(fakeCredential)
            )

        assertSoftly {
            maybeOutput.shouldNotBeNull()
            maybeOutput.value.let { result ->
                result.shouldBeTypeOf<Authenticated>()
                result.user.shouldBe(fakeUser)
            }
        }
    }
})

internal data class TestUser(override val displayName: String, override val id: UserUid) : User

internal class TestAuthService(
    private val onAttemptAuthentication: (suspend (Credential) -> Result<User, AuthError>)? = null,
    private val onSignOut: (() -> Unit)? = null,
    private val onGetSignedInUserIfAny: (suspend () -> Result<User?, AuthError>)? = null
) : AuthService {
    override suspend fun attemptAuthentication(credential: Credential): Result<User, AuthError> =
        onAttemptAuthentication?.invoke(credential)
            ?: Err(AuthError("Can't sign in with $credential"))

    override fun signOut() {
        onSignOut?.invoke()
    }

    override suspend fun getSignedInUserIfAny(): Result<User?, AuthError> {
        return onGetSignedInUserIfAny?.invoke() ?: Ok(null)
    }
}
