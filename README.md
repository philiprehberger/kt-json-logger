# json-logger

[![CI](https://github.com/philiprehberger/kt-json-logger/actions/workflows/publish.yml/badge.svg)](https://github.com/philiprehberger/kt-json-logger/actions/workflows/publish.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.philiprehberger/json-logger)](https://central.sonatype.com/artifact/com.philiprehberger/json-logger)

Structured JSON logging with context fields and sensitive field masking.

## Requirements

- Kotlin 1.9+ / Java 17+

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("com.philiprehberger:json-logger:0.1.0")
}
```

### Maven

```xml
<dependency>
    <groupId>com.philiprehberger</groupId>
    <artifactId>json-logger</artifactId>
    <version>0.1.0</version>
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

## API

| Function / Class | Description |
|------------------|-------------|
| `jsonLogger(name)` | Create a structured JSON logger |
| `logger.info(message) { fields }` | Log with structured fields |
| `logger.error(message, throwable) { fields }` | Log errors with stack trace |
| `JsonLogger.addGlobalField(key, value)` | Add field to every log entry |
| `JsonLogger.maskFields(vararg fields)` | Mark fields for masking |
| `JsonLogger.minLevel` | Set minimum log level |
| `LogLevel` | DEBUG, INFO, WARN, ERROR |

## Development

```bash
./gradlew test
./gradlew build
```

## License

MIT
