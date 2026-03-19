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
}
