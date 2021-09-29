package com.omricat.maplibrarian.utils

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import kotlinx.coroutines.CancellationException

/**
 * If this result is failure with an [Err.error] that is an instance of the type
 * [ExceptionT] then throw that [Throwable].
 */
public inline fun <V, reified E : Exception> Result<V, Throwable>.except(): Result<V, Throwable> =
    when (this) {
        is Ok<*> -> this
        is Err<Throwable> -> {
            if (error is E) throw error else this
        }
    }

/**
 * Calls the specified function [block] and returns its encapsulated result if
 * invocation was successful, catching and encapsulating any thrown exception
 * as a failure.
 */
@Suppress("RedundantSuspendModifier")
public suspend inline fun <V> runSuspendCatching(
    block: () -> V
): Result<V, Throwable> = runCatching(block).except<V, CancellationException>()

/**
 * Calls the specified function [block] with [this] value as its receiver and
 * returns its encapsulated result if invocation was successful, catching and
 * encapsulating any thrown exception as a failure.
 *
 * Rethrows CancellationException so that coroutines running within [block]
 * will be correctly cancelled.
 */
@Suppress("RedundantSuspendModifier")
public suspend inline infix fun <T, V> T.runSuspendCatching(
    block: T.() -> V
): Result<V, Throwable> = this.runCatching(block).except<V, CancellationException>()
