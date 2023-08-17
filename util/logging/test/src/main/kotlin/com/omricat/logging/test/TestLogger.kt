package com.omricat.logging.test

import co.touchlab.kermit.Severity
import co.touchlab.kermit.Tag
import com.omricat.logging.Logger

public class TestLogger : Logger {

    private val logs: List<LogLine>
        get() = _logs

    private val _logs = mutableListOf<LogLine>()

    override fun log(priority: Severity, tag: Tag, throwable: Throwable?, message: () -> String) {
        _logs.add(LogLine(priority, tag, throwable, message()))
    }

    public data class LogLine(
        public val priority: Severity,
        public val tag: Tag,
        public val throwable: Throwable?,
        public val message: String
    )
}
