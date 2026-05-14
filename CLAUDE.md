# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./gradlew build
./gradlew bootJar          # produces build/libs/app.jar

# Run locally
./gradlew bootRun

# Test
./gradlew test
./gradlew test --tests WealthmapApplicationTests
./gradlew test --tests "com.example.demo.SomeTest.methodName"

# Clean
./gradlew clean
```

## Architecture

Spring Boot 3.2.4 REST API with MySQL, deployed on Railway. The app uses stateless JWT authentication, Spring Data JPA with Hibernate DDL-auto (no migration tool), and Server-Sent Events for real-time notifications.

**Layer structure** under `src/main/java/com/example/demo/`:

| Package | Role |
|---|---|
| `controller/` | 19 `@RestController` classes — one per domain |
| `service/` | Business logic; some methods are `@Async` (email) |
| `repository/` | Spring Data JPA repositories extending `JpaRepository` |
| `entity/` | JPA entities annotated with Lombok `@Data` |
| `dto/` | Request/response DTOs for controller boundaries |
| `vo/` | `AppResponse<T>` — universal response wrapper; `RspCode` enum |
| `security/` | `JwtTokenProvider`, `JwtAuthenticationFilter`, `CustomUserDetailsService` |
| `constant/` | `RiskLevel` enum (CONSERVATIVE, DEFENSIVE, GROWTH) with allocation percentages |
| `scheduler/` | Scheduled tasks (`@EnableScheduling` on main class) for stock/news data updates |
| `config/` | `SecurityConfig` (JWT + CORS), `CorsConfig` |

**All API responses** use `AppResponse<T>` from `vo/AppResponse.java`. Controllers should never return raw types.

**Key domains:** User auth, asset portfolio, risk assessment & strategy, financial goals, debt/liability, cash flow, Monte Carlo simulation, portfolio rebalancing, Taiwan stock data, news feed (GNews + FinMind APIs), SSE notifications.

## Environment Variables

The app reads configuration from environment variables (Railway injects these in production). For local development, set them in your shell or a `.env` file:

| Variable | Purpose |
|---|---|
| `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD` | MySQL connection |
| `PORT` | Server port (defaults to `8080`) |
| `JWT_SECRET` | JWT signing secret |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins |
| `GNEWS_API_KEY` | GNews API for news fetching |
| `FINMIND_API_TOKEN` | FinMind API for Taiwan stock data |
| `MAILJET_API_KEY` | Mailjet API key for sending emails |
| `MAILJET_SECRET_KEY` | Mailjet secret key for sending emails |
| `MAILJET_FROM_ADDRESS` | Verified sender address in Mailjet (single sender verification, can be a Gmail) |

## Database

MySQL via Spring Data JPA. Schema is managed by `spring.jpa.hibernate.ddl-auto=update` — Hibernate auto-creates/updates tables on startup. There is no Flyway or Liquibase.

## Deployment

Deployed on Railway. The `Procfile` defines the start command:
```
web: java -Xmx512m -Dserver.port=$PORT -jar build/libs/app.jar
```
The Gradle build is configured to output the JAR as `build/libs/app.jar`.
