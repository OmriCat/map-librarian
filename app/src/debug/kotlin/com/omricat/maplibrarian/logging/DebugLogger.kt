package com.omricat.maplibrarian.logging

import co.touchlab.kermit.BaseLogger
import co.touchlab.kermit.LoggerConfig
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Severity.Debug
import co.touchlab.kermit.Tag
import co.touchlab.kermit.loggerConfigInit
import co.touchlab.kermit.platformLogWriter
import com.omricat.logging.Logger

class DebugLogger(
    minimumPriority: Severity = Debug,
    config: LoggerConfig = loggerConfigInit(platformLogWriter(), minSeverity = minimumPriority),
) : Logger, BaseLogger(config) {
    override fun log(priority: Severity, tag: Tag, throwable: Throwable?, message: () -> String) {
        logBlock(priority, tag.tag, throwable, message)
    }
}
