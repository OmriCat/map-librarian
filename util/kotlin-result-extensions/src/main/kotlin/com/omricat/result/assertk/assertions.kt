package com.omricat.result.assertk

import assertk.Assert
import assertk.assertions.support.appendName
import assertk.assertions.support.expected
import assertk.assertions.support.show
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError

public fun <V> Assert<Result<V, *>>.isOk(): Assert<V> =
    transform(appendName("value", separator = ".")) { actualResult ->
        if (actualResult is Ok<V>) {
            actualResult.value
        } else {
            expected(
                "to be Ok but was ${show(actualResult)} with error ${show(actualResult.getError())}"
            )
        }
    }

//    isInstanceOf<Ok<V>>().transform { it.value }

public fun <E> Assert<Result<*, E>>.isErr(): Assert<E> =
    transform(appendName("error", separator = ".")) { actualResult ->
        if (actualResult is Err<E>) {
            actualResult.error
        } else {
            expected(
                "to be Err but was ${show(actualResult)} with value ${show(actualResult.get())}"
            )
        }
    }
