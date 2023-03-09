package com.omricat.maplibrarian.firebase.auth

import androidx.test.core.app.ApplicationProvider
import com.github.michaelbull.result.Ok
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.omricat.maplibrarian.auth.EmailPasswordCredential
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.utils.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

// Connects to host computer from Android emulator
private const val FIREBASE_EMULATOR_HOST = "10.0.2.2"

private const val FIREBASE_EMULATOR_AUTH_PORT = 9099

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseAuthServiceTest {

    @Before
    fun clearUsers() {
        emulatorAPI.deleteAllUsers()
    }

    @Test
    fun addUserSucceeds() {
        val credential = EmailPasswordCredential("test@example.com", "password")

        runTest {
            val repository =
                FirebaseAuthService(firebaseAuthInstance, TestDispatcherProvider(testScheduler))
            val createUserResult = repository.createUser(credential)
            assert(createUserResult is Ok<User>)
        }
    }

    companion object Fixtures {

        @JvmStatic lateinit var firebaseAuthInstance: FirebaseAuth

        @JvmStatic lateinit var emulatorAPI: FirebaseAuthEmulatorRestApi

        @JvmStatic
        @BeforeClass
        fun setUp() {
            val app: FirebaseApp =
                FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
                    ?: error("Failed to initialize FirebaseApp")
            firebaseAuthInstance =
                FirebaseAuth.getInstance(app).apply {
                    useEmulator(FIREBASE_EMULATOR_HOST, FIREBASE_EMULATOR_AUTH_PORT)
                }
            emulatorAPI =
                FirebaseAuthEmulatorRestApi(
                    app.options.projectId!!,
                    HttpUrl.Builder()
                        .host(FIREBASE_EMULATOR_HOST)
                        .port(FIREBASE_EMULATOR_AUTH_PORT)
                        .scheme("http")
                        .build()
                )
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherProvider(scheduler: TestCoroutineScheduler) : DispatcherProvider {
    private val dispatcher = StandardTestDispatcher(scheduler)
    override val default: CoroutineDispatcher
        get() = dispatcher
    override val io: CoroutineDispatcher
        get() = dispatcher
    override val main: CoroutineDispatcher
        get() = dispatcher
    override val unconfined: CoroutineDispatcher
        get() = dispatcher
}
