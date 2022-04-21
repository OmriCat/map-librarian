package com.omricat.maplibrarian.chartlist

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.omricat.maplibrarian.auth.AuthError
import com.omricat.maplibrarian.auth.AuthService
import com.omricat.maplibrarian.auth.Credential
import com.omricat.maplibrarian.auth.EmailPasswordCredential
import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.maplibrarian.model.UnsavedChartModel
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.model.UserUid
import com.omricat.maplibrarian.utils.DispatcherProvider.DefaultDispatcherProvider
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class FirebaseChartsServiceIntegrationTest {

    @get:Rule
    val emulator = FirebaseEmulatorContainer()

    private lateinit var firestoreInstance: FirebaseFirestore

    private val testScheduler = TestCoroutineScheduler()

    private val testDispatcherProvider = object : DefaultDispatcherProvider() {
        private val dispatcher = StandardTestDispatcher(testScheduler)
        override val default: CoroutineDispatcher
            get() = dispatcher
        override val main: CoroutineDispatcher
            get() = dispatcher
        override val unconfined: CoroutineDispatcher
            get() = dispatcher
        override val io: CoroutineDispatcher
            get() = dispatcher
    }

    /*
        See https://github.com/Kotlin/kotlinx.coroutines/issues/3173
     */
    @Before
    fun coroutinesWorkaround() {
        System.setProperty("kotlinx.coroutines.main.delay", "false")
    }

    @Before
    fun beforeTest() {
        firestoreInstance =
            FirebaseFirestore.getInstance(
                FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())!!
            ).apply {
                firestoreSettings = firestoreSettings {
                    isPersistenceEnabled = false
                }
                useEmulator(emulator.host, emulator.getMappedPort(8080))
            }
    }

    @After
    fun afterTest() {
        firestoreInstance.terminate()
    }

    @Test
    fun insertAndQuery() {
        val service = FirebaseChartsService(firestoreInstance, testDispatcherProvider)
        runTest(context = testDispatcherProvider.io) {

            // Add charts to repository
            for (i in 1..3) {
                service.addNewChart(user, UnsavedChartModel(user.id, "Chart $i"))
            }

            // Read back from repository
            val chartsResult = service.chartsListForUser(user)
            chartsResult.shouldBeInstanceOf<Ok<List<DbChartModel>>>()
            chartsResult.value.should {
                it.shouldHaveSize(3)
            }
        }
    }

    @Test
    fun `create user then add chart and query added chart`() {
        val authService: AuthService = TestAuthService()

        val chartsService = FirebaseChartsService(firestoreInstance, testDispatcherProvider)

        runTest(context = testScheduler, dispatchTimeoutMs = 15_000) {
            val createUserResult: Result<User, AuthError> =
                authService.createUser(EmailPasswordCredential("user1@example.com", "password"))
            createUserResult.shouldBeInstanceOf<Ok<User>>()
            val user = createUserResult.value

            chartsService.addNewChart(user, UnsavedChartModel(user.id, "Chart"))
                .shouldBeInstanceOf<Ok<DbChartModel>>()

            val queryChartsResult: Result<List<DbChartModel>, ChartsServiceError> =
                chartsService.chartsListForUser(user)

            queryChartsResult.shouldBeInstanceOf<Ok<List<DbChartModel>>>()
            queryChartsResult.value.should {
                it.shouldHaveSize(1)
            }
        }
    }
}

private val user = object : User {
    override val displayName: String = "User 1"
    override val id: UserUid = UserUid("XbmeeiiyihVjM5aBtVT0frNiGall")
}

internal class TestAuthService : AuthService {
    override suspend fun attemptAuthentication(credential: Credential): Result<User, AuthError> =
        throw UnsupportedOperationException()

    override fun signOut(): Unit = throw UnsupportedOperationException()

    override suspend fun getSignedInUserIfAny(): Result<User?, AuthError> =
        throw UnsupportedOperationException()

    override suspend fun createUser(credential: Credential): Result<User, AuthError> =
        Ok(user)
}
