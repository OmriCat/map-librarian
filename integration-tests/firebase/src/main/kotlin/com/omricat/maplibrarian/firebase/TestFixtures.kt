package com.omricat.maplibrarian.firebase

import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import java.io.IOException
import java.util.concurrent.TimeUnit
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

object TestFixtures {
    val app: FirebaseApp by lazy {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
            ?: error("Failed to initialize FirebaseApp")
    }

    val projectId: String
        get() = app.options.projectId ?: error("Can't get projectId from FirebaseApp options")

    fun emulatorBaseUrl(port: Int): HttpUrl =
        Builder().host(FirebaseEmulatorConnection.HOST).port(port).scheme("http").build()

    @OptIn(ExperimentalTime::class)
    class OkHttpTimingEventListener(
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

        private fun log(call: Call, data: String) =
            "[${TimeSource.Monotonic.markNow()}]: ${call.request().url} | $data"

        override fun responseBodyEnd(call: Call, byteCount: Long) {
            readTime = readingStartTimestamp.elapsedNow()
            output("responseBodyEnd(): $call, ${call.request()}, read time: $readTime")
        }

        override fun responseFailed(call: Call, ioe: IOException) {
            output("responseFailed(): $call, ${call.request()}, $ioe")
        }
    }

    @OptIn(ExperimentalTime::class)
    fun okHttpClient(output: (String) -> Unit, timeSource: TimeSource = TimeSource.Monotonic) =
        OkHttpClient.Builder()
            .callTimeout(OKHTTP_CALL_TIMEOUT, SECONDS)
            .readTimeout(OKHTTP_READ_TIMEOUT, TimeUnit.SECONDS)
            .eventListener(OkHttpTimingEventListener(output = output, timeSource = timeSource))
            .build()
}
