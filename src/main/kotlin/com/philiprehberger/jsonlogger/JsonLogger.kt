package com.philiprehberger.jsonlogger

import java.time.Instant

/** Create a JSON logger with the given name. */
public fun jsonLogger(name: String): JsonLogger = JsonLogger(name)

/** Structured JSON logger. */
public class JsonLogger(private val name: String) {
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

    public fun debug(message: String, block: (LogFieldBuilder.() -> Unit)? = null) = log(LogLevel.DEBUG, message, null, block)
    public fun info(message: String, block: (LogFieldBuilder.() -> Unit)? = null) = log(LogLevel.INFO, message, null, block)
    public fun warn(message: String, block: (LogFieldBuilder.() -> Unit)? = null) = log(LogLevel.WARN, message, null, block)
    public fun error(message: String, throwable: Throwable? = null, block: (LogFieldBuilder.() -> Unit)? = null) = log(LogLevel.ERROR, message, throwable, block)

    private fun log(level: LogLevel, message: String, throwable: Throwable?, block: (LogFieldBuilder.() -> Unit)?) {
        if (level.ordinal < minLevel.ordinal) return
        val fields = mutableMapOf<String, Any?>()
        fields.putAll(globalFields)
        if (block != null) { val fb = LogFieldBuilder(); fb.block(); fields.putAll(fb.fields) }
        // Mask sensitive fields
        for (key in maskedFields) { if (fields.containsKey(key)) fields[key] = "***" }

        val json = buildString {
            append("{")
            append(""timestamp":"${Instant.now()}",")
            append(""level":"$level",")
            append(""logger":"${escapeJson(name)}",")
            append(""message":"${escapeJson(message)}"")
            for ((k, v) in fields) { append(","${escapeJson(k)}":${toJsonValue(v)}") }
            if (throwable != null) {
                append(","error":{"type":"${throwable::class.simpleName}","message":"${escapeJson(throwable.message ?: "")}"}")
            }
            append("}")
        }
        output(json)
    }

    private fun escapeJson(s: String): String = s.replace("\\", "\\\\").replace(""", "\\"").replace("\n", "\\n")
    private fun toJsonValue(v: Any?): String = when (v) {
        null -> "null"
        is String -> ""${escapeJson(v)}""
        is Number, is Boolean -> v.toString()
        else -> ""${escapeJson(v.toString())}""
    }
}

/** Builder for structured log fields. */
public class LogFieldBuilder {
    internal val fields = mutableMapOf<String, Any?>()
    /** Add a field. */
    public infix fun String.to(value: Any?) { fields[this] = value }
}
