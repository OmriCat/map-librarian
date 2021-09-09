package com.omricat.workflow

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.squareup.workflow1.Worker
import com.squareup.workflow1.asWorker
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

private typealias AsyncFn<T, R> = suspend (T) -> R
private typealias AsyncProducer<R> = suspend () -> R

fun <I, V, E> AsyncFn<I, Result<V, E>>.asResultWorker(
    errorWrapper: (Throwable) -> E
): (I) -> Worker<Result<V, E>> = { input ->
    flow { emit(this@asResultWorker.invoke(input)) }
        .catch { e: Throwable ->
            emit(Err(errorWrapper(e)))
        }.asWorker()
}

fun <V, E> AsyncProducer<Result<V, E>>.asResultWorker(
    errorWrapper: (Throwable) -> E
): Worker<Result<V, E>> =
    flow { emit(this@asResultWorker.invoke()) }
        .catch { e: Throwable ->
            emit(Err(errorWrapper(e)))
        }.asWorker()
