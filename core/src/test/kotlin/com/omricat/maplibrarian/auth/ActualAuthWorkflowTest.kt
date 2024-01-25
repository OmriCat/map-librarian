package com.omricat.maplibrarian.auth

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import com.omricat.maplibrarian.auth.ActualAuthWorkflow.State
import com.omricat.maplibrarian.auth.ActualAuthWorkflow.State.LoginPrompt
import com.omricat.maplibrarian.auth.AuthResult.Authenticated
import com.omricat.maplibrarian.model.UserUid
import com.omricat.maplibrarian.workflow.assertk.value
import com.squareup.workflow1.StatelessWorkflow
import com.squareup.workflow1.applyTo
import kotlin.test.Test

public class ActualAuthWorkflowTest {
    @Test
    fun `onAuthError action returns to login prompt`() {
        val workflow = ActualAuthWorkflow(TestUserRepository(), NullSignupWorkflow)
        val fakeCredential = EmailPasswordCredential("a@b.com", "12345")
        val (newState, maybeOutput) =
            workflow
                .onAuthError(MessageError("Authentication failure"))
                .applyTo(props = Unit, state = State.AttemptingAuthorization(fakeCredential))

        assertThat(maybeOutput).isNull()
        assertThat(newState)
            .isInstanceOf<LoginPrompt>()
            .prop(LoginPrompt::errorMessage)
            .isNotEmpty()
    }

    @Test
    fun `onNoAuthenticatedUser action transitions to LoginPrompt`() {
        val workflow = ActualAuthWorkflow(TestUserRepository(), NullSignupWorkflow)
        val (newState, maybeOutput) =
            workflow.onNoAuthenticatedUser.applyTo(props = Unit, state = State.PossibleLoggedInUser)

        assertThat(maybeOutput).isNull()
        assertThat(newState).isInstanceOf<LoginPrompt>()
    }

    @Test
    fun `onAuthenticated action outputs Authenticated(user) from workflow`() {
        val workflow = ActualAuthWorkflow(TestUserRepository(), NullSignupWorkflow)
        val fakeCredential = EmailPasswordCredential("a@b.com", "12345")
        val fakeUser = TestUser("user1", UserUid("1"), "blah@example.com")
        val (_, maybeOutput) =
            workflow
                .onAuthenticated(fakeUser)
                .applyTo(props = Unit, state = State.AttemptingAuthorization(fakeCredential))

        assertThat(maybeOutput)
            .isNotNull()
            .value()
            .isInstanceOf<Authenticated>()
            .prop(Authenticated::user)
            .isEqualTo(fakeUser)
    }
}

private object NullSignupWorkflow :
    SignUpWorkflow, StatelessWorkflow<Unit, SignUpOutput, SignUpScreen>() {
    override @Test fun render(renderProps: Unit, context: RenderContext): SignUpScreen =
        object : SignUpScreen {}
}
