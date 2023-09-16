package com.omricat.maplibrarian.firebase.auth

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.omricat.maplibrarian.auth.CreateUserError
import com.omricat.maplibrarian.auth.EmailPasswordCredential
import com.omricat.maplibrarian.auth.FirebaseUserRepository
import com.omricat.maplibrarian.firebase.TestDispatcherProvider
import com.omricat.maplibrarian.firebase.TestFixtures.authApi
import com.omricat.maplibrarian.firebase.TestFixtures.firebaseAuthInstance
import com.omricat.maplibrarian.firebase.auth.FirebaseAuthEmulatorRestApi.TestUser
import com.omricat.result.assertk.isErr
import com.omricat.result.assertk.isOk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

@RunWith(Enclosed::class)
class FirebaseUserRepositoryTest {

    @Before
    fun clearUsers() {
        authApi.deleteAllUsers()
    }

    private val testCredential = EmailPasswordCredential("test@example.com", "password")

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
        val createdUser = createUserViaRestApi(testCredential)
        val repository =
            FirebaseUserRepository(firebaseAuthInstance, TestDispatcherProvider(testScheduler))
        val signInResult = repository.attemptAuthentication(testCredential)
        assertThat(signInResult)
            .isOk()
            .prop("Email address") { it.emailAddress.value }
            .isEqualTo(createdUser.email)
    }

    private fun createUserViaRestApi(credential: EmailPasswordCredential): TestUser =
        authApi.createUser(credential).body()
            ?: error("Failed to create user with credentials $credential")
}
