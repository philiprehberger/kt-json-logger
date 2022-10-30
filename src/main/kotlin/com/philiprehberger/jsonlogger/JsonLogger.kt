package com.philiprehberger.jsonlogger

import java.time.Instant

/** Create a JSON logger with the given name. */
public fun jsonLogger(name: String): JsonLogger = JsonLogger(name)

/** Structured JSON logger. */
public class JsonLogger(private val name: String) {
    private val contextFields = mutableMapOf<String, Any?>()

    public companion object {
        /** Minimum log level. */
        public var minLevel: LogLevel = LogLevel.DEBUG
        /** Output function (default: println). */
        public var output: (String) -> Unit = ::println
        private val globalFields = mutableMapOf<String, Any?>()
        private val maskedFields = mutableSetOf<String>()

        /** Add a field to every log entry. */
        public fun addGlobalField(key: String, value: Any?) { globalFields[key] = value }
        /** Mark fields for masking. */
        public fun maskFields(vararg fields: String) { maskedFields.addAll(fields) }
        /** Clear all global config (for testing). */
        public fun reset() { globalFields.clear(); maskedFields.clear(); minLevel = LogLevel.DEBUG }
    }

    /** Log at DEBUG level. */
    public fun debug(message: String, block: (LogFieldBuilder.() -> Unit)? = null): Unit = log(LogLevel.DEBUG, message, null, block)
    /** Log at INFO level. */
    public fun info(message: String, block: (LogFieldBuilder.() -> Unit)? = null): Unit = log(LogLevel.INFO, message, null, block)
    /** Log at WARN level. */
    public fun warn(message: String, block: (LogFieldBuilder.() -> Unit)? = null): Unit = log(LogLevel.WARN, message, null, block)
    /** Log at ERROR level. */
    public fun error(message: String, throwable: Throwable? = null, block: (LogFieldBuilder.() -> Unit)? = null): Unit = log(LogLevel.ERROR, message, throwable, block)

    /**
     * Creates a child logger with additional context fields baked in.
     *
     * All logs from the child logger will automatically include the given fields.
     *
     * @param fields key-value pairs to include in every log entry from this logger
     * @return a new [JsonLogger] with the additional context
     */
    public fun withContext(vararg fields: Pair<String, Any?>): JsonLogger {
        val child = JsonLogger(name)
        child.contextFields.putAll(this.contextFields)
        child.contextFields.putAll(fields)
        return child
    }

    /**
     * Logs at the given level and measures the duration of [block].
     *
     * The log entry includes a `duration_ms` field with the elapsed time in milliseconds.
     *
     * @param T the return type of the block
     * @param level the log level
     * @param message the log message
     * @param block the block to measure
     * @return the result of [block]
     */
    public fun <T> timed(level: LogLevel = LogLevel.INFO, message: String, block: () -> T): T {
        val start = System.currentTimeMillis()
        val result = block()
        val duration = System.currentTimeMillis() - start
        log(level, message, null) { "duration_ms" to duration }
        return result
    }

    private fun log(level: LogLevel, message: String, throwable: Throwable?, block: (LogFieldBuilder.() -> Unit)?) {
        if (level.ordinal < minLevel.ordinal) return
        val fields = mutableMapOf<String, Any?>()
        fields.putAll(globalFields)
        fields.putAll(contextFields)
        if (block != null) { val fb = LogFieldBuilder(); fb.block(); fields.putAll(fb.fields) }
        for (key in maskedFields) { if (fields.containsKey(key)) fields[key] = "***" }

        val json = buildString {
            append("{\"timestamp\":\"")
            append(Instant.now())
            append("\",\"level\":\"")
            append(level)
            append("\",\"logger\":\"")
            append(escapeJson(name))
            append("\",\"message\":\"")
            append(escapeJson(message))
            append("\"")
            for ((k, v) in fields) {
                append(",\"")
                append(escapeJson(k))
                append("\":")
                append(toJsonValue(v))
            }
            if (throwable != null) {
                append(",\"error\":{\"type\":\"")
                append(throwable::class.simpleName)
                append("\",\"message\":\"")
                append(escapeJson(throwable.message ?: ""))
                append("\"}")
            }
            append("}")
        }
        output(json)
    }

    private fun escapeJson(s: String): String = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
    private fun toJsonValue(v: Any?): String = when (v) {
        null -> "null"
        is String -> "\"${escapeJson(v)}\""
        is Number, is Boolean -> v.toString()
        else -> "\"${escapeJson(v.toString())}\""
    }
}

/** Builder for structured log fields. */
public class LogFieldBuilder {
    internal val fields: MutableMap<String, Any?> = mutableMapOf()
    /** Add a field. */
    public infix fun String.to(value: Any?) { fields[this] = value }
}
