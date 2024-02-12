package com.omricat.maplibrarian.firebase.charts

import assertk.assertThat
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.unwrap
import com.omricat.logging.test.TestLogger
import com.omricat.maplibrarian.auth.EmailPasswordCredential
import com.omricat.maplibrarian.auth.FirebaseUserRepository
import com.omricat.maplibrarian.chartlist.ChartsRepository
import com.omricat.maplibrarian.chartlist.FirebaseChartsRepository
import com.omricat.maplibrarian.firebase.TestDispatcherProvider
import com.omricat.maplibrarian.firebase.TestFixtures
import com.omricat.maplibrarian.model.DbChartModel
import com.omricat.maplibrarian.model.UnsavedChartModel
import com.omricat.maplibrarian.model.User
import com.omricat.maplibrarian.utils.DispatcherProvider.DefaultDispatcherProvider
import com.omricat.result.assertk.isOk
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

@OptIn(ExperimentalTime::class)
class FirebaseChartsRepositoryTest {

    @Before
    fun clearData() {
        TestFixtures.firestoreApi.deleteAllData()
    }

    @Test
    fun addChartThenEdit() = runTest {
        val testDispatcherProvider = TestDispatcherProvider(testScheduler)
        val testLogger = TestLogger()

        val chartsRepository =
            FirebaseChartsRepository(
                TestFixtures.firestoreInstance,
                testDispatcherProvider,
                testLogger,
            )

        val addChartResult = chartsRepository.addNewChart(user, UnsavedChartModel("Chart title"))

        assertThat(addChartResult).isOk()

        val createdChart: DbChartModel = addChartResult.unwrap()

        val editChartResult: Result<DbChartModel, ChartsRepository.SaveEditedChartError> =
            chartsRepository.saveEditedChart(
                user,
                createdChart.chartId,
                UnsavedChartModel("A different title")
            )

        assertThat(editChartResult).isOk()
    }

    companion object {

        lateinit var user: User
            private set

        @JvmStatic
        @BeforeClass
        fun createUser(): Unit {
            TestFixtures.authApi.deleteAllUsers()
            runBlocking {
                val repository =
                    FirebaseUserRepository(
                        TestFixtures.firebaseAuthInstance,
                        UnconfinedDispatcherProvider
                    )
                user =
                    repository
                        .createUser(EmailPasswordCredential("test@example.com", "password"))
                        .get() ?: throw IllegalStateException("Unable to create test user")
            }
        }

        private object UnconfinedDispatcherProvider : DefaultDispatcherProvider() {
            override val default: CoroutineDispatcher
                get() = Dispatchers.Unconfined

            override val io: CoroutineDispatcher
                get() = Dispatchers.Unconfined

            override val main: CoroutineDispatcher
                get() = Dispatchers.Unconfined

            override val unconfined: CoroutineDispatcher
                get() = Dispatchers.Unconfined
        }
    }
}
