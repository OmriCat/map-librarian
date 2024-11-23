package com.omricat.logging

import co.touchlab.kermit.Severity
import co.touchlab.kermit.Severity.Debug
import co.touchlab.kermit.Tag
import kotlin.reflect.KClass

/** Convenience "mixin" interface to make logging easy. */
public interface Loggable {
    public val logger: Logger
    public val loggingTag: Tag
}

public inline fun <reified T : Any> T.classTag(): Tag = Tag(T::class.outerClassSimpleName())

public fun Loggable.log(priority: Severity = Debug, tag: String? = null, message: () -> String) {
    logger.log(priority, tag?.let { Tag(it) } ?: this.loggingTag, message)
}

public fun Loggable.log(
    priority: Severity = Debug,
    tag: String? = null,
    throwable: Throwable,
    message: () -> String = { throwable.message ?: "$throwable" },
) {
    logger.log(priority, tag?.let { Tag(it) } ?: this.loggingTag, throwable, message)
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
