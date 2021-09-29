package com.omricat.maplibrarian.utils

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onFailure
import timber.log.Timber

inline fun <V, E> Result<V, Throwable>.logErrorAndMap(
    transform: (Throwable) -> E
): Result<V, E> =
    this.onFailure { Timber.w(it) }.mapError(transform)
