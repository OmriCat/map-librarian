package com.omricat.maplibrarian.utils

import co.touchlab.kermit.Severity.Warn
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onFailure
import com.omricat.logging.Loggable
import com.omricat.logging.log

context(Loggable)

inline fun <V, E> Result<V, Throwable>.logErrorAndMap(transform: (Throwable) -> E): Result<V, E> =
    this.onFailure { logger.log(Warn, throwable = it) { "" } }.mapError(transform)
