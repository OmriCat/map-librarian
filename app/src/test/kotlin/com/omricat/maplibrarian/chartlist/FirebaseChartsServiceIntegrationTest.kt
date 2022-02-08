package com.omricat.maplibrarian.chartlist

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.michaelbull.result.Ok
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestoreSettings
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.testcontainers.containers.FirestoreEmulatorContainer
import org.testcontainers.utility.DockerImageName

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class FirebaseChartsServiceIntegrationTest {

    @get:Rule
    val emulator =
        FirestoreEmulatorContainer(
            DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:367.0.0-emulators")
        )

    private lateinit var firestoreInstance: FirebaseFirestore

    private val testScheduler = TestCoroutineScheduler()

    private val dispatcherProvider = object : DefaultDispatcherProvider() {
        override val io: CoroutineDispatcher
            get() = StandardTestDispatcher(testScheduler)
    }

    private val user = object : User {
        override val displayName: String = "User 1"
        override val id: UserUid = UserUid("XbmeeiiyihVjM5aBtVT0frNiGall")
    }

    @Before
    fun beforeTest() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        firestoreInstance = FirebaseFirestore.getInstance().apply {
            firestoreSettings = firestoreSettings {
                isPersistenceEnabled = false
            }
            useEmulator(emulator.host, emulator.getMappedPort(8080))
        }
    }

    @Test
    fun insertAndQuery() {
        val service = FirebaseChartsService(firestoreInstance, dispatcherProvider)
        runTest(context = dispatcherProvider.io) {

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
}
