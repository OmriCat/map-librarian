package com.omricat.maplibrarian.auth

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.omricat.maplibrarian.auth.ActualAuthWorkflow.State.LoginPrompt
import com.omricat.maplibrarian.auth.AuthScreen.AttemptingLogin
import com.omricat.maplibrarian.model.User
import com.squareup.workflow1.testing.testRender
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

public class ActualAuthWorkflowTest : StringSpec({
    "AuthWorkflow render" {
        val workflow = ActualAuthWorkflow(TestAuthService())
        workflow.testRender(Unit).render { screen ->
            screen.shouldBeInstanceOf<AttemptingLogin>()
        }.verifyActionResult { newState, output ->
            newState.shouldBe(LoginPrompt())
            output.shouldBeNull()
        }
    }
})

private class TestAuthService(
    private val onAttemptAuthentication: (suspend (Credential) -> Result<User, AuthError>)? = null,
    private val onSignOut: (() -> Unit)? = null,
    private val onGetSignedInUserIfAny: (suspend () -> Result<User, AuthError>)? = null
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
