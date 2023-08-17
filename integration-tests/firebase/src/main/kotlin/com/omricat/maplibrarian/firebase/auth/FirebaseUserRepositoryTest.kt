package com.omricat.maplibrarian.firebase.auth

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.google.firebase.auth.FirebaseAuth
import com.omricat.maplibrarian.auth.CreateUserError
import com.omricat.maplibrarian.auth.EmailPasswordCredential
import com.omricat.maplibrarian.auth.FirebaseUserRepository
import com.omricat.maplibrarian.firebase.FirebaseEmulatorConnection
import com.omricat.maplibrarian.firebase.TestDispatcherProvider
import com.omricat.maplibrarian.firebase.TestFixtures
import com.omricat.maplibrarian.firebase.auth.FirebaseAuthEmulatorRestApi.TestUser
import com.omricat.result.assertk.isErr
import com.omricat.result.assertk.isOk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseUserRepositoryTest {

    @Before
    fun clearUsers() {
        authApi.deleteAllUsers()
    }

    private val testCredential = EmailPasswordCredential("test@example.com", "password")

    class CreateUserTest {
        @Test
        fun addUserSucceeds() = runTest {
            val repository =
                FirebaseUserRepository(firebaseAuthInstance, TestDispatcherProvider(testScheduler))
            val createUserResult =
                repository.createUser(EmailPasswordCredential("test@example.com", "password"))
            assertThat(createUserResult).isOk()
        }

        @Test
        fun tooShortPasswordGivesError() = runTest {
            val repository =
                FirebaseUserRepository(firebaseAuthInstance, TestDispatcherProvider(testScheduler))
            val createUserResult =
                repository.createUser(EmailPasswordCredential("test@example.com", "pw"))
            assertThat(createUserResult).isErr().isInstanceOf<CreateUserError.WeakPasswordError>()
        }

        @Test
        fun emailCollisionGivesError() = runTest {
            val repository =
                FirebaseUserRepository(firebaseAuthInstance, TestDispatcherProvider(testScheduler))
            val firstCreateUserResult =
                repository.createUser(EmailPasswordCredential("test@example.com", "password"))
            val secondCreateUserResult =
                repository.createUser(EmailPasswordCredential("test@example.com", "password"))
            assertAll {
                assertThat(firstCreateUserResult).isOk()
                assertThat(secondCreateUserResult)
                    .isErr()
                    .isInstanceOf<CreateUserError.EmailAlreadyInUseError>()
            }
        }
    }

    @Test
    fun canSignInAddedUser() = runTest {
        val repository =
            FirebaseUserRepository(firebaseAuthInstance, TestDispatcherProvider(testScheduler))
        val createUserResult = repository.createUser(testCredential)
        repository.signOut()
        val signInResult = repository.attemptAuthentication(testCredential)
        assertThat(signInResult).isOk()
    }

    @Test
    fun canSignInExternallyAddedUser() = runTest {
        val createdUser = Fixtures.createUserViaRestApi(testCredential)
        val repository =
            FirebaseUserRepository(firebaseAuthInstance, TestDispatcherProvider(testScheduler))
        val signInResult = repository.attemptAuthentication(testCredential)
        assertThat(signInResult)
            .isOk()
            .prop("Email address") { it.emailAddress.value }
            .isEqualTo(createdUser.email)
    }

    companion object Fixtures {

        @JvmStatic lateinit var firebaseAuthInstance: FirebaseAuth

        @JvmStatic lateinit var authApi: FirebaseAuthEmulatorRestApi

        @JvmStatic
        @BeforeClass
        fun setUp() {
            firebaseAuthInstance =
                FirebaseAuth.getInstance(TestFixtures.app).apply {
                    useEmulator(
                        FirebaseEmulatorConnection.HOST,
                        FirebaseEmulatorConnection.AUTH_PORT
                    )
                }
            authApi =
                FirebaseAuthEmulatorRestApi(
                    TestFixtures.projectId,
                    TestFixtures.emulatorBaseUrl(FirebaseEmulatorConnection.AUTH_PORT)
                )
        }

        fun createUserViaRestApi(credential: EmailPasswordCredential): TestUser =
            authApi.createUser(credential).body()
                ?: error("Failed to create user with credentials $credential")
    }
}
