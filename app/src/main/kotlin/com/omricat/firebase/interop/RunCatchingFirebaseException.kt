package com.omricat.firebase.interop

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.google.firebase.FirebaseException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
public inline fun <V, reified E : FirebaseException> runCatchingFirebaseException(
    block: () -> V
): Result<V, E> {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

    return try {
        Ok(block())
    } catch (e: FirebaseException) {

        // Can't use generics in catch so suppress the detekt check
        @Suppress("detekt:InstanceOfCheckForException") if (e !is E) throw e else Err(e)
    }
}
