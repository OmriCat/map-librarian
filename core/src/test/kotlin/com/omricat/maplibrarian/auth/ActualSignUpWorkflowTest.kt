package com.omricat.maplibrarian.auth

import assertk.all
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import com.omricat.maplibrarian.auth.SignUpOutput.SignUpCancelled
import com.omricat.maplibrarian.auth.SignUpOutput.UserCreated
import com.omricat.maplibrarian.auth.SignUpScreen.EmailAndPasswordSignUpScreen
import com.omricat.maplibrarian.auth.State.AttemptingUserCreation
import com.omricat.maplibrarian.auth.State.SignUpPrompt
import com.omricat.maplibrarian.model.UserUid
import com.omricat.maplibrarian.workflow.assertk.value
import com.squareup.workflow1.applyTo
import com.squareup.workflow1.testing.testRender
import kotlin.test.Test
import org.junit.jupiter.api.Nested

internal class ActualSignUpWorkflowTest {

    @Nested
    inner class Actions {
        @Test
        fun `onSignUpClicked sets correct state and no output`() {
            val workflow = ActualSignUpWorkflow(TestUserRepository())
            val credential = EmailPasswordCredential("blah@blah", "password")

            val (newState, maybeOutput) =
                workflow.onSignUpClicked(credential).applyTo(props = Unit, state = SignUpPrompt())

            assertThat(maybeOutput).isNull()
            assertThat(newState).isInstanceOf<AttemptingUserCreation>()
        }

        @Test
        fun `onErrorCreatingUser sets correct state and no output`() {
            val workflow = ActualSignUpWorkflow(TestUserRepository())
            val credential = EmailPasswordCredential("blah@blah", "password")

            val (newState, maybeOutput) =
                workflow
                    .onErrorCreatingUser(credential, MessageError("Error creating user"))
                    .applyTo(props = Unit, state = AttemptingUserCreation(credential))

            assertThat(maybeOutput).isNull()
            assertThat(newState).isInstanceOf<SignUpPrompt>()
        }

        @Test
        fun `onUserCreated sets output to created user`() {
            val workflow = ActualSignUpWorkflow(TestUserRepository())
            val credential = EmailPasswordCredential("blah@blah", "password")
            val user = TestUser("user1", UserUid("1234"), "blah@example.com")

            val (_, maybeOutput) =
                workflow
                    .onUserCreated(user)
                    .applyTo(props = Unit, state = AttemptingUserCreation(credential))

            assertThat(maybeOutput)
                .isNotNull()
                .value()
                .isInstanceOf<UserCreated>()
                .prop(UserCreated::user)
                .isEqualTo(user)
        }

        @Test
        fun `onSignUpCancelled sets output to null applied to AttemptingUserCreation state`() {
            val workflow = ActualSignUpWorkflow(TestUserRepository())
            val credential = EmailPasswordCredential("blah@blah", "password")

            val (_, maybeOutput) =
                workflow
                    .onSignUpCancelled()
                    .applyTo(props = Unit, state = AttemptingUserCreation(credential))

            assertThat(maybeOutput).isNotNull().value().isInstanceOf<SignUpCancelled>()
        }

        @Test
        fun `onSignUpCancelled sets output to null applied to SignUpPrompt state`() {
            val workflow = ActualSignUpWorkflow(TestUserRepository())

            val (_, maybeOutput) =
                workflow.onSignUpCancelled().applyTo(props = Unit, state = SignUpPrompt())

            assertThat(maybeOutput).isNotNull().value().isInstanceOf<SignUpCancelled>()
        }
    }

    @Nested
    inner class Rendering {
        @Test
        fun `be cancellable`() {
            val workflow = ActualSignUpWorkflow(TestUserRepository())
            workflow
                .testRender(props = Unit)
                .render { screen ->
                    assertThat(screen)
                        .isInstanceOf<EmailAndPasswordSignUpScreen>()
                        .prop("credential") { it.credential }
                        .all {
                            prop("emailAddress") { it.emailAddress }.isEmpty()
                            prop("password") { it.password }.isEmpty()
                        }

                    screen as EmailAndPasswordSignUpScreen
                    screen.backPressHandler.invoke()
                }
                .verifyActionResult { _, output ->
                    assertThat(output).isNotNull().value().isInstanceOf<SignUpCancelled>()
                }
        }
    }
}
