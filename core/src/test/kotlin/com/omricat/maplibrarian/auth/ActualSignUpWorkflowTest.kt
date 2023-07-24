package com.omricat.maplibrarian.auth

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

internal class ActualSignUpWorkflowTest :
    WordSpec({
        "actions work correctly" should
            {
                "onSignUpClicked sets correct state and no output" {
                    val workflow = ActualSignUpWorkflow(TestAuthService())
                    val credential = EmailPasswordCredential("blah@blah", "password")

                    val (newState, maybeOutput) =
                        workflow
                            .onSignUpClicked(credential)
                            .applyTo(props = Unit, state = SignUpPrompt())

                    assertSoftly {
                        maybeOutput.shouldBeNull()
                        newState.shouldBeTypeOf<State.AttemptingUserCreation>()
                    }
                }

                "onErrorCreatingUser sets correct state and no output" {
                    val workflow = ActualSignUpWorkflow(TestAuthService())
                    val credential = EmailPasswordCredential("blah@blah", "password")

                    val (newState, maybeOutput) =
                        workflow
                            .onErrorCreatingUser(credential, AuthError("Error creating user"))
                            .applyTo(props = Unit, state = AttemptingUserCreation(credential))

                    assertSoftly {
                        maybeOutput.shouldBeNull()
                        newState.shouldBeTypeOf<State.SignUpPrompt>()
                    }
                }

                "onUserCreated sets output to created user" {
                    val workflow = ActualSignUpWorkflow(TestAuthService())
                    val credential = EmailPasswordCredential("blah@blah", "password")
                    val user = TestUser("user1", UserUid("1234"), "blah@example.com")

                    val (_, maybeOutput) =
                        workflow
                            .onUserCreated(user)
                            .applyTo(props = Unit, state = AttemptingUserCreation(credential))

                    assertSoftly {
                        maybeOutput.shouldNotBeNull()
                        maybeOutput.value.also {
                            it.shouldBeTypeOf<SignUpOutput.UserCreated>()
                            it.user.shouldBe(user)
                        }
                    }
                }

                "onSignUpCancelled sets output to null applied to AttemptingUserCreation state" {
                    val workflow = ActualSignUpWorkflow(TestAuthService())
                    val credential = EmailPasswordCredential("blah@blah", "password")

                    val (_, maybeOutput) =
                        workflow
                            .onSignUpCancelled()
                            .applyTo(props = Unit, state = AttemptingUserCreation(credential))

                    assertSoftly {
                        maybeOutput.shouldNotBeNull()
                        maybeOutput.value.shouldBe(SignUpOutput.SignUpCancelled)
                    }
                }

                "onSignUpCancelled sets output to null applied to SignUpPrompt state" {
                    val workflow = ActualSignUpWorkflow(TestAuthService())

                    val (_, maybeOutput) =
                        workflow.onSignUpCancelled().applyTo(props = Unit, state = SignUpPrompt())

                    assertSoftly {
                        maybeOutput.shouldNotBeNull()
                        maybeOutput.value.shouldBe(SignUpOutput.SignUpCancelled)
                    }
                }
            }

        "rendering ActualSignUpWorkflow" should
            {
                "be cancellable" {
                    val workflow = ActualSignUpWorkflow(TestAuthService())
                    workflow
                        .testRender(props = Unit)
                        .render { screen ->
                            screen.shouldBeTypeOf<SignUpScreen.EmailAndPasswordSignUpScreen>()
                            screen.credential.also {
                                it.emailAddress.shouldBeEmpty()
                                it.password.shouldBeEmpty()
                            }
                            screen.backPressHandler.invoke()
                        }
                        .verifyActionResult { _, output ->
                            output.shouldNotBeNull()
                            output.value.shouldBe(SignUpOutput.SignUpCancelled)
                        }
                }
            }
    })
