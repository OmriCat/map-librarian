package com.omricat.workflow

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.squareup.workflow1.Worker
import com.squareup.workflow1.asWorker
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

public fun <V, E> resultWorker(
    exceptionWrapper: (Throwable) -> E,
    body: suspend () -> Result<V, E>
): Worker<Result<V, E>> =
    flow {
        emit(body())
    }.catch { e -> emit(Err(exceptionWrapper(e))) }
        .asWorker()
