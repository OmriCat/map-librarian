package com.omricat.maplibrarian.firebase.auth

import com.github.michaelbull.result.Ok
import com.google.firebase.auth.FirebaseAuth
import com.omricat.maplibrarian.auth.EmailPasswordCredential
import com.omricat.maplibrarian.firebase.FirebaseEmulatorConnection
import com.omricat.maplibrarian.firebase.TestDispatcherProvider
import com.omricat.maplibrarian.firebase.TestFixtures
import com.omricat.maplibrarian.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseAuthServiceTest {

    @Before
    fun clearUsers() {
        authApi.deleteAllUsers()
    }

    private val testCredential = EmailPasswordCredential("test@example.com", "password")

    @Test
    fun addUserSucceeds() = runTest {
        val repository =
            FirebaseAuthService(firebaseAuthInstance, TestDispatcherProvider(testScheduler))
        val createUserResult = repository.createUser(testCredential)
        assert(createUserResult is Ok<User>)
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
    }
}
