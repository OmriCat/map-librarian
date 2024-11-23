package com.omricat.logging

import co.touchlab.kermit.Severity
import co.touchlab.kermit.Severity.Debug
import co.touchlab.kermit.Tag

/**
 * Facade for logging. Presents a simplified interface on top of more fully-featured logging
 * implementations.
 */
public interface Logger {
    public fun log(
        priority: Severity = Debug,
        tag: Tag,
        throwable: Throwable?,
        message: () -> String,
    )

    public fun log(priority: Severity = Debug, tag: Tag, message: () -> String): Unit =
        log(priority, tag, null, message)

    public companion object NoOpLogger : Logger {
        override fun log(
            priority: Severity,
            tag: Tag,
            throwable: Throwable?,
            message: () -> String,
        ): Unit = Unit
    }
}
