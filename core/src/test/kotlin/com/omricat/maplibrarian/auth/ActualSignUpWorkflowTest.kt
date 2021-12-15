package com.omricat.maplibrarian.auth

import com.omricat.maplibrarian.auth.ActualSignUpWorkflow.Actions
import com.omricat.maplibrarian.auth.State.AttemptingUserCreation
import com.omricat.maplibrarian.auth.State.SignUpPrompt
import com.omricat.maplibrarian.model.UserUid
import com.squareup.workflow1.applyTo
import com.squareup.workflow1.testing.testRender
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.matchers.types.shouldBeTypeOf

internal class ActualSignUpWorkflowTest : WordSpec({

    "actions work correctly" should {
        "onSignUpClicked sets correct state and no output" {
            val credential = EmailPasswordCredential("blah@blah", "password")

            val (newState, maybeOutput) = Actions.OnSignUpClicked(credential).applyTo(
                props = Unit,
                state = SignUpPrompt()
            )

            assertSoftly {
                maybeOutput.shouldBeNull()
                newState.shouldBeTypeOf<AttemptingUserCreation>()
            }
        }

        "onErrorCreatingUser sets correct state and no output" {
            val credential = EmailPasswordCredential("blah@blah", "password")

            val (newState, maybeOutput) = Actions.OnErrorCreatingUser(
                credential,
                AuthError("Error creating user")
            ).applyTo(
                props = Unit,
                state = AttemptingUserCreation(credential)
            )

            assertSoftly {
                maybeOutput.shouldBeNull()
                newState.shouldBeTypeOf<SignUpPrompt>()
            }
        }

        "onUserCreated sets output to created user" {
            val credential = EmailPasswordCredential("blah@blah", "password")
            val user = TestUser("user1", UserUid("1234"))

            val (_, maybeOutput) = Actions.OnUserCreated(user).applyTo(
                props = Unit,
                state = AttemptingUserCreation(credential)
            )

            assertSoftly {
                maybeOutput.shouldNotBeNull()
                maybeOutput.value.also {
                    it.shouldBeTypeOf<SignUpOutput.UserCreated>()
                    it.user.shouldBe(user)
                }
            }
        }

        "onSignUpCancelled sets output to null applied to AttemptingUserCreation state" {
            val credential = EmailPasswordCredential("blah@blah", "password")

            val (_, maybeOutput) = Actions.OnSignUpCancelled.applyTo(
                props = Unit,
                state = AttemptingUserCreation(credential)
            )

            assertSoftly {
                maybeOutput.shouldNotBeNull()
                maybeOutput.value.shouldBe(SignUpOutput.SignUpCancelled)
            }
        }

        "onSignUpCancelled sets output to null applied to SignUpPrompt state" {
            val (_, maybeOutput) = Actions.OnSignUpCancelled.applyTo(
                props = Unit,
                state = SignUpPrompt()
            )

            assertSoftly {
                maybeOutput.shouldNotBeNull()
                maybeOutput.value.shouldBe(SignUpOutput.SignUpCancelled)
            }
        }
    }

    "rendering ActualSignUpWorkflow" should {
        "be cancellable" {
            val workflow = ActualSignUpWorkflow(TestAuthService())
            workflow.testRender(props = Unit)
                .render { screen ->
                    screen.shouldBeTypeOf<SignUpScreen.EmailAndPasswordSignUpScreen>()
                    screen.credential.also {
                        it.emailAddress.shouldBeEmpty()
                        it.password.shouldBeEmpty()
                    }
                    screen.backPressHandler.invoke()
                }.verifyAction {
                    it.shouldBeTypeOf<Actions.OnSignUpCancelled>()
                }
        }
    }
})
