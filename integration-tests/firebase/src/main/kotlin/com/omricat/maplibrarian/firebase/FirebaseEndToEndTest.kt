package com.omricat.maplibrarian.firebase

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import com.github.michaelbull.result.getOrThrow
import com.omricat.logging.test.TestLogger
import com.omricat.maplibrarian.auth.EmailPasswordCredential
import com.omricat.maplibrarian.auth.FirebaseUserRepository
import com.omricat.maplibrarian.chartlist.FirebaseChartsRepository
import com.omricat.maplibrarian.firebase.TestFixtures.authApi
import com.omricat.maplibrarian.firebase.TestFixtures.firebaseAuthInstance
import com.omricat.maplibrarian.firebase.TestFixtures.firestoreApi
import com.omricat.maplibrarian.firebase.TestFixtures.firestoreInstance
import com.omricat.maplibrarian.model.UnsavedChartModel
import com.omricat.result.assertk.isOk
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@Suppress("FunctionName")
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
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
        val userRepository = FirebaseUserRepository(firebaseAuthInstance, testDispatcherProvider)
        val testLogger = TestLogger()

        val chartsRepository =
            FirebaseChartsRepository(
                firestoreInstance,
                testDispatcherProvider,
                testLogger,
            )

        val createUserResult = userRepository.createUser(testCredential)

        val user = createUserResult.getOrThrow { AssertionError(it) }

        val addChartResult = chartsRepository.addNewChart(user, UnsavedChartModel("New map"))

        assertThat(addChartResult).isOk()

        val queryChartResult = chartsRepository.chartsListForUser(user)

        assertThat(queryChartResult).isOk().all {
            hasSize(1)
            prop("First item title") { it.first().title }.isEqualTo("New map")
        }
    }
}
