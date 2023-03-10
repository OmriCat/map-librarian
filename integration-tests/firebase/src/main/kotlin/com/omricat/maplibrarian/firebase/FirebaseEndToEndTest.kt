package com.omricat.maplibrarian.firebase

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrThrow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.omricat.maplibrarian.auth.EmailPasswordCredential
import com.omricat.maplibrarian.chartlist.FirebaseChartsService
import com.omricat.maplibrarian.firebase.auth.FirebaseAuthEmulatorRestApi
import com.omricat.maplibrarian.firebase.auth.FirebaseAuthService
import com.omricat.maplibrarian.firebase.charts.FirebaseFirestoreRestApi
import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.maplibrarian.model.UnsavedChartModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseEndToEndTest {

    @Before
    fun clearData() {
        authApi.deleteAllUsers()
        firestoreApi.deleteAllData()
    }

    private val testCredential = EmailPasswordCredential("test@example.com", "password")

    @Test
    fun addUser_addCharts_queryCharts() = runTest {
        val testDispatcherProvider = TestDispatcherProvider(testScheduler)
        val userRepository = FirebaseAuthService(firebaseAuthInstance, testDispatcherProvider)

        val chartsRepository = FirebaseChartsService(firestoreInstance, testDispatcherProvider)

        val createUserResult = userRepository.createUser(testCredential)

        val user = createUserResult.getOrThrow { AssertionError(it) }

        val addChartResult =
            chartsRepository.addNewChart(user, UnsavedChartModel(user.id, "New map"))

        assert(addChartResult is Ok<DbChartModel>)

        val queryChartResult = chartsRepository.chartsListForUser(user)

        queryChartResult
            .getOrThrow { AssertionError(it) }
            .run {
                assert(size == 1)
                assert(first().title == "New map")
            }
    }

    companion object Fixtures {

        @JvmStatic lateinit var firestoreInstance: FirebaseFirestore

        @JvmStatic lateinit var firestoreApi: FirebaseFirestoreRestApi

        @JvmStatic lateinit var firebaseAuthInstance: FirebaseAuth

        @JvmStatic lateinit var authApi: FirebaseAuthEmulatorRestApi

        @JvmStatic
        @BeforeClass
        fun setup() {
            firestoreInstance =
                FirebaseFirestore.getInstance(TestFixtures.app).apply {
                    useEmulator(
                        FirebaseEmulatorConnection.HOST,
                        FirebaseEmulatorConnection.FIRESTORE_PORT
                    )
                }

            firestoreApi =
                FirebaseFirestoreRestApi(
                    TestFixtures.projectId,
                    TestFixtures.emulatorBaseUrl(FirebaseEmulatorConnection.FIRESTORE_PORT)
                )

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
