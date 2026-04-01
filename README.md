# json-logger

[![Tests](https://github.com/philiprehberger/kt-json-logger/actions/workflows/publish.yml/badge.svg)](https://github.com/philiprehberger/kt-json-logger/actions/workflows/publish.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.philiprehberger/json-logger.svg)](https://central.sonatype.com/artifact/com.philiprehberger/json-logger)
[![Last updated](https://img.shields.io/github/last-commit/philiprehberger/kt-json-logger)](https://github.com/philiprehberger/kt-json-logger/commits/main)

Structured JSON logging with context fields and sensitive field masking.

## Installation

### Gradle (Kotlin DSL)

```kotlin
implementation("com.philiprehberger:json-logger:0.2.0")
```

### Maven

```xml
<dependency>
    <groupId>com.philiprehberger</groupId>
    <artifactId>json-logger</artifactId>
    <version>0.2.0</version>
</dependency>
```

## Usage

```kotlin
import com.philiprehberger.jsonlogger.*

val logger = jsonLogger("UserService")

logger.info("User created") {
    "userId" to "u-123"
    "email" to "alice@example.com"
}
// {"timestamp":"...","level":"INFO","logger":"UserService","message":"User created","userId":"u-123","email":"alice@example.com"}

JsonLogger.maskFields("password", "token")
JsonLogger.addGlobalField("service", "user-api")
```

### Context Logger

```kotlin
val logger = jsonLogger("api")
val requestLogger = logger.withContext("request_id" to requestId, "user_id" to userId)

requestLogger.info("Processing request")
// {"timestamp":"...","level":"INFO","logger":"api","message":"Processing request","request_id":"abc123","user_id":"42"}
```

### Timed Operations

```kotlin
val result = logger.timed(message = "Database query") {
    database.query("SELECT * FROM users")
}
// {"timestamp":"...","level":"INFO","message":"Database query","duration_ms":45}
```

## API

| Function / Class | Description |
|------------------|-------------|
| `jsonLogger(name)` | Create a structured JSON logger |
| `logger.info(message) { fields }` | Log with structured fields |
| `logger.error(message, throwable) { fields }` | Log errors with stack trace |
| `JsonLogger.addGlobalField(key, value)` | Add field to every log entry |
| `JsonLogger.maskFields(vararg fields)` | Mark fields for masking |
| `JsonLogger.withContext()` | Create child logger with baked-in fields |
| `JsonLogger.timed()` | Log with automatic duration measurement |
| `JsonLogger.minLevel` | Set minimum log level |
| `LogLevel` | DEBUG, INFO, WARN, ERROR |

## Development

```bash
./gradlew test
./gradlew build
```

## Support

If you find this project useful:

⭐ [Star the repo](https://github.com/philiprehberger/kt-json-logger)

🐛 [Report issues](https://github.com/philiprehberger/kt-json-logger/issues?q=is%3Aissue+is%3Aopen+label%3Abug)

💡 [Suggest features](https://github.com/philiprehberger/kt-json-logger/issues?q=is%3Aissue+is%3Aopen+label%3Aenhancement)

❤️ [Sponsor development](https://github.com/sponsors/philiprehberger)

🌐 [All Open Source Projects](https://philiprehberger.com/open-source-packages)

💻 [GitHub Profile](https://github.com/philiprehberger)

🔗 [LinkedIn Profile](https://www.linkedin.com/in/philiprehberger)

## License

[MIT](LICENSE)
