package com.omricat.result.kotest.assertk

import assertk.Assert
import assertk.assertions.isInstanceOf
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result

public fun <V> Assert<Result<V, *>>.isOk(): Assert<V> = isInstanceOf<Ok<V>>().transform { it.value }

public fun <E> Assert<Result<*, E>>.isErr(): Assert<E> =
    isInstanceOf<Err<E>>().transform { it.error }
