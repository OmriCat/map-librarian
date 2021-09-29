package com.omricat.maplibrarian.utils

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.runCatching
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
internal class RunSuspendCatchingTest : StringSpec({

    "runSuspendCatching is cancellable" {
        val testDispatcher = TestCoroutineDispatcher()
        val testScope = TestCoroutineScope(testDispatcher)

        testScope.runBlockingTest {

            var value: String? = null

            launch {
                launch {
                    val result = runSuspendCatching {
                        delay(Duration.seconds(4))
                        "value"
                    }

                    result.onSuccess { value = it }
                }

                testDispatcher.advanceTimeBy(2000)

                cancel()
            }

            value.shouldBeNull()
        }
    }

    "suspend functions run in runBlocking can't be cancelled" {
        val testDispatcher = TestCoroutineDispatcher()
        val testScope = TestCoroutineScope(testDispatcher)

        testScope.runBlockingTest {

            var value: String? = null

            launch {
                launch {
                    val result: Result<String, Throwable> = runCatching {
                        delay(Duration.seconds(4))
                        "value"
                    }
                    result.onFailure { value = "Failure" }
                }

                testDispatcher.advanceTimeBy(2000)

                cancel()
            }

            value.shouldNotBeNull()
        }
    }
}
)
