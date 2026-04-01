package com.philiprehberger.jsonlogger

import kotlin.test.*

class JsonLoggerTest {
    @BeforeTest fun setup() { JsonLogger.reset() }

    @Test fun `basic log output`() {
        var output = ""
        JsonLogger.output = { output = it }
        val logger = jsonLogger("test")
        logger.info("hello")
        assertTrue(output.contains("\"message\":\"hello\""))
        assertTrue(output.contains("\"level\":\"INFO\""))
    }
    @Test fun `fields attached`() {
        var output = ""
        JsonLogger.output = { output = it }
        jsonLogger("test").info("msg") { "userId" to "123" }
        assertTrue(output.contains("\"userId\":\"123\""))
    }
    @Test fun `masking`() {
        var output = ""
        JsonLogger.output = { output = it }
        JsonLogger.maskFields("password")
        jsonLogger("test").info("login") { "password" to "secret" }
        assertTrue(output.contains("\"password\":\"***\""))
        assertFalse(output.contains("secret"))
    }
    @Test fun `level filtering`() {
        var called = false
        JsonLogger.output = { called = true }
        JsonLogger.minLevel = LogLevel.WARN
        jsonLogger("test").debug("skip")
        assertFalse(called)
    }

    @Test
    fun `withContext adds fields to every log`() {
        val output = mutableListOf<String>()
        JsonLogger.output = { output.add(it) }
        JsonLogger.minLevel = LogLevel.DEBUG

        val logger = jsonLogger("test").withContext("request_id" to "abc123")
        logger.info("hello")

        assertTrue(output[0].contains("request_id"))
        assertTrue(output[0].contains("abc123"))

        JsonLogger.reset()
    }

    @Test
    fun `withContext inherits parent context`() {
        val output = mutableListOf<String>()
        JsonLogger.output = { output.add(it) }
        JsonLogger.minLevel = LogLevel.DEBUG

        val parent = jsonLogger("test").withContext("service" to "api")
        val child = parent.withContext("request_id" to "xyz")
        child.info("test")

        assertTrue(output[0].contains("service"))
        assertTrue(output[0].contains("request_id"))

        JsonLogger.reset()
    }

    @Test
    fun `timed logs with duration_ms`() {
        val output = mutableListOf<String>()
        JsonLogger.output = { output.add(it) }
        JsonLogger.minLevel = LogLevel.DEBUG

        val logger = jsonLogger("test")
        val result = logger.timed(message = "operation") { 42 }

        assertEquals(42, result)
        assertEquals(1, output.size)
        assertTrue(output[0].contains("duration_ms"))
        assertTrue(output[0].contains("operation"))

        JsonLogger.reset()
    }
}
