package com.omricat.result.kotest

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import io.kotest.matchers.Matcher
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.contracts.contract

fun <V, E> ok() = Matcher<Result<V, E>> { result ->
    io.kotest.matchers.MatcherResult(
        passed = result.get() != null,
        failureMessageFn = { "result was Err but we expected Ok" },
        negatedFailureMessageFn = { "result should not be Ok" },
    )
}

fun <V, E> Result<V, E>.shouldBeOk(): Ok<V> {
    contract {
        returns() implies (this@shouldBeOk is Ok<V>)
    }
    val matcher = ok<V, E>()
    this.shouldBe(matcher)
    return this as Ok<V>
}

inline fun <V, E> Result<V, E>.shouldBeOk(block: (V) -> Unit) = shouldBeOk().value.also(block)

fun <V, E> Result<V, E>.shouldBeErr(): Err<E> {
    contract {
        returns() implies (this@shouldBeErr is Err<E>)
    }
    val matcher = ok<V, E>()
    this.shouldNotBe(matcher)
    return this as Err<E>
}

inline fun <V, E> Result<V, E>.shouldBeErr(block: (E) -> Unit) = shouldBeErr().error.also(block)
