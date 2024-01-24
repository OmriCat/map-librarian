package com.omricat.logging

import co.touchlab.kermit.Severity
import co.touchlab.kermit.Severity.Debug
import co.touchlab.kermit.Tag
import kotlin.reflect.KClass

/** Convenience "mixin" interface to make logging easy. */
public interface Loggable {
    public val logger: Logger
}

context(Loggable)
public inline fun <reified T : Any> T.log(
    priority: Severity = Debug,
    tag: String? = null,
    noinline message: () -> String
) {
    logger.log(priority, Tag(tag ?: T::class.outerClassSimpleName()), message)
}

context(Loggable)
public inline fun <reified T : Any> T.log(
    priority: Severity = Debug,
    tag: String? = null,
    throwable: Throwable,
    noinline message: () -> String = { throwable.message ?: "$throwable" }
) {
    logger.log(priority, Tag(tag ?: T::class.outerClassSimpleName()), throwable, message)
}

public fun KClass<*>.outerClassSimpleName(): String {
    val fullClassName = this.java.name
    val outerClassName = fullClassName.substringBefore('$')
    val simplerOuterClassName = outerClassName.substringAfterLast('.')
    return if (simplerOuterClassName.isEmpty()) {
        fullClassName
    } else {
        simplerOuterClassName.removeSuffix("Kt")
    }
}
