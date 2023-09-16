package com.omricat.maplibrarian.firebase

import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.omricat.maplibrarian.firebase.auth.FirebaseAuthEmulatorRestApi
import com.omricat.maplibrarian.firebase.charts.FirebaseFirestoreRestApi
import java.io.IOException
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Builder
import okhttp3.OkHttpClient
import okhttp3.Request

private const val OKHTTP_CALL_TIMEOUT = 20L

private const val OKHTTP_READ_TIMEOUT = 15L

@OptIn(ExperimentalTime::class)
object TestFixtures {
    private val app: FirebaseApp by lazy {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
            ?: error("Failed to initialize FirebaseApp")
    }

    private val projectId: String
        get() = app.options.projectId ?: error("Can't get projectId from FirebaseApp options")

    val firestoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance(app).apply {
            this.useEmulator(
                FirebaseEmulatorConnection.HOST,
                FirebaseEmulatorConnection.FIRESTORE_PORT
            )
        }
    }

    val firestoreApi: FirebaseFirestoreRestApi by lazy {
        FirebaseFirestoreRestApi(
            projectId,
            emulatorBaseUrl(FirebaseEmulatorConnection.FIRESTORE_PORT)
        )
    }

    val firebaseAuthInstance: FirebaseAuth by lazy {
        FirebaseAuth.getInstance(app).apply {
            useEmulator(FirebaseEmulatorConnection.HOST, FirebaseEmulatorConnection.AUTH_PORT)
        }
    }

    val authApi: FirebaseAuthEmulatorRestApi by lazy {
        FirebaseAuthEmulatorRestApi(
            projectId,
            emulatorBaseUrl(FirebaseEmulatorConnection.AUTH_PORT)
        )
    }

    private fun emulatorBaseUrl(port: Int): HttpUrl =
        Builder().host(FirebaseEmulatorConnection.HOST).port(port).scheme("http").build()

    internal class OkHttpTimingEventListener(
        private val timeSource: TimeSource,
        private val output: (String) -> Unit,
    ) : EventListener() {

        private lateinit var requestSentTimestamp: TimeMark
        private lateinit var readingStartTimestamp: TimeMark
        private var requestToResponseLatency: Duration? = null
        private var readTime: Duration? = null

        override fun requestHeadersEnd(call: Call, request: Request) {
            requestSentTimestamp = timeSource.markNow()
            output("requestHeadersEnd(): $call, $request")
        }

        override fun responseHeadersStart(call: Call) {
            readingStartTimestamp = timeSource.markNow()
            requestToResponseLatency = requestSentTimestamp.elapsedNow()
            output(
                "responseHeadersStart(): ${call}, ${call.request()}, latency: $requestToResponseLatency"
            )
        }

        override fun responseBodyEnd(call: Call, byteCount: Long) {
            readTime = readingStartTimestamp.elapsedNow()
            output("responseBodyEnd(): $call, ${call.request()}, read time: $readTime")
        }

        override fun responseFailed(call: Call, ioe: IOException) {
            output("responseFailed(): $call, ${call.request()}, $ioe")
        }
    }

    fun okHttpClient(output: (String) -> Unit, timeSource: TimeSource = TimeSource.Monotonic) =
        OkHttpClient.Builder()
            .callTimeout(OKHTTP_CALL_TIMEOUT, SECONDS)
            .readTimeout(OKHTTP_READ_TIMEOUT, SECONDS)
            .eventListener(OkHttpTimingEventListener(output = output, timeSource = timeSource))
            .build()
}
