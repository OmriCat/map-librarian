package com.omricat.maplibrarian.firebase

import android.annotation.SuppressLint
import android.app.Instrumentation
import android.os.Bundle
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import com.github.michaelbull.result.getOrThrow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.omricat.maplibrarian.auth.EmailPasswordCredential
import com.omricat.maplibrarian.auth.FirebaseUserRepository
import com.omricat.maplibrarian.chartlist.FirebaseChartsService
import com.omricat.maplibrarian.firebase.auth.FirebaseAuthEmulatorRestApi
import com.omricat.maplibrarian.firebase.charts.FirebaseFirestoreRestApi
import com.omricat.maplibrarian.model.UnsavedChartModel
import com.omricat.result.assertk.isOk
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

@Suppress("FunctionName")
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class FirebaseEndToEndTest {

    @Before
    fun clearData() {
        authApi.deleteAllUsers()
        firestoreApi.deleteAllData()
    }

    @After
    fun reportRestApiMetrics() {
        InstrumentationRegistry.getInstrumentation()
            .sendStatus(
                0,
                Bundle().apply {
                    putString(Instrumentation.REPORT_KEY_STREAMRESULT, "\n${firestoreApi.events}")
                }
            )
        Log.i(FirebaseEndToEndTest::class.simpleName, firestoreApi.events.toString())
    }

    private val testCredential = EmailPasswordCredential("test@example.com", "password")

    @Test
    fun addUser_addCharts_queryCharts() = runTest {
        val testDispatcherProvider = TestDispatcherProvider(testScheduler)
        val userRepository = FirebaseUserRepository(firebaseAuthInstance, testDispatcherProvider)

        val chartsRepository = FirebaseChartsService(firestoreInstance, testDispatcherProvider)

        val createUserResult = userRepository.createUser(testCredential)

        val user = createUserResult.getOrThrow { AssertionError(it) }

        val addChartResult =
            chartsRepository.addNewChart(user, UnsavedChartModel(user.id, "New map"))

        assertThat(addChartResult).isOk()

        val queryChartResult = chartsRepository.chartsListForUser(user)

        assertThat(queryChartResult).isOk().all {
            hasSize(1)
            prop("First item title") { it.first().title }.isEqualTo("New map")
        }
    }

    companion object Fixtures {

        // Not a problem to leak a Context in an instrumented test
        @SuppressLint("StaticFieldLeak")
        @JvmStatic
        lateinit var firestoreInstance: FirebaseFirestore

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
