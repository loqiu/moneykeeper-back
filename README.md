[**CN 中文**](./README_zh.md) | [**GB English**](./README.md)

# MoneyKeeper Backend

MoneyKeeper Backend is a Spring Boot 3 service for personal and shared ledger management. It provides APIs for authentication, ledger collaboration, categories, records, statistics, budgets, notifications, export jobs, payment flows, and search.

## Highlights

- Personal and shared ledgers with member roles and invite flows
- Ledger-scoped categories, records, summaries, and statistics
- Budget management with threshold rules and notification logs
- Excel export jobs with async processing
- Elasticsearch-backed record search
- Kafka-based record and export job event flow, with local fallback
- Stripe payment integration
- Flyway migrations for schema evolution

## Tech Stack

- Java 17
- Spring Boot 3.2
- MyBatis-Plus 3.5
- MySQL 8
- Redis
- Elasticsearch
- Kafka
- Flyway
- Log4j2
- JWT
- Springdoc OpenAPI

## Project Structure

```text
src/main/java/com/loqiu/moneykeeper/
|- config/               application and integration config
|- controller/           REST endpoints
|- dto/                  response DTOs
|- entity/               MyBatis-Plus entities
|- exception/            exception mapping and error handling
|- health/               actuator integration health checks
|- interceptor/          request interceptors
|- mapper/               MyBatis mapper interfaces
|- service/              service contracts
|- service/impl/         business logic
|- util/                 shared utilities
|- vo/                   request models

src/main/resources/
|- application*.properties
|- db/migration/         Flyway SQL migrations
|- mapper/               MyBatis XML
```

## Core Modules

- Authentication: username/password login, JWT, user profile APIs
- Ledger collaboration: default personal ledgers, shared ledgers, members, invites, accept-invite flow
- Categories and records: ledger-scoped CRUD plus summaries
- Statistics: weekly, monthly, yearly ledger analytics
- Budgets: monthly budgets, threshold rules, progress tracking
- Notifications: unread counts, mark-as-read, budget alerts, export-ready notifications
- Export jobs: async Excel generation and download
- Search: Elasticsearch indexing and ledger/user record search
- Payments: membership plans, checkout, subscriptions, webhook handling

## Documentation

- Frontend API contract: [FRONTEND_API.md](./FRONTEND_API.md)
- Frontend handoff summary: [PLATFORM_FRONTEND_HANDOFF.md](./PLATFORM_FRONTEND_HANDOFF.md)
- Deployment helpers: [deploy/README.md](./deploy/README.md)
- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`

## Quick Start

### Prerequisites

- JDK 17+
- Maven 3.9+
- MySQL 8+
- Redis

Optional but recommended for the full feature set:

- Elasticsearch
- Kafka

### Local Run

1. Create a MySQL database:

```sql
CREATE DATABASE moneykeeper CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Set environment variables as needed:

```powershell
$env:MONEYKEEPER_DB_URL="jdbc:mysql://localhost:3306/moneykeeper?useUnicode=true&characterEncoding=utf8&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:MONEYKEEPER_DB_USERNAME="root"
$env:MONEYKEEPER_DB_PASSWORD="your-password"
$env:MONEYKEEPER_REDIS_HOST="localhost"
$env:MONEYKEEPER_REDIS_PORT="6379"
$env:SPRING_PROFILES_ACTIVE="dev"
```

3. Start the application:

```powershell
mvn spring-boot:run
```

4. Verify health:

```powershell
curl http://localhost:8081/actuator/health
```

Flyway runs automatically on startup.

## Configuration

Important settings are environment-driven. Common keys:

- `SPRING_PROFILES_ACTIVE`
- `MONEYKEEPER_DB_URL`
- `MONEYKEEPER_DB_USERNAME`
- `MONEYKEEPER_DB_PASSWORD`
- `MONEYKEEPER_REDIS_HOST`
- `MONEYKEEPER_REDIS_PORT`
- `MONEYKEEPER_REDIS_DATABASE`
- `MONEYKEEPER_ELASTICSEARCH_ENABLED`
- `MONEYKEEPER_ELASTICSEARCH_HOST`
- `MONEYKEEPER_ELASTICSEARCH_PORT`
- `MONEYKEEPER_ELASTICSEARCH_INDEX_NAME`
- `MONEYKEEPER_KAFKA_ENABLED`
- `MONEYKEEPER_KAFKA_BOOTSTRAP_SERVERS`
- `MONEYKEEPER_KAFKA_RECORD_EVENT_TOPIC`
- `MONEYKEEPER_KAFKA_EXPORT_JOB_TOPIC`
- `MONEYKEEPER_EXPORT_JOB_STORAGE_DIR`
- `MONEYKEEPER_PAYMENT_ENABLED`
- `MONEYKEEPER_PAYMENT_SECRET_KEY`
- `MONEYKEEPER_PAYMENT_WEBHOOK_SECRET`

For full defaults and integration toggles, see:

- [src/main/resources/application.properties](./src/main/resources/application.properties)
- [src/main/resources/application-dev.properties](./src/main/resources/application-dev.properties)
- [src/main/resources/application-prod.properties](./src/main/resources/application-prod.properties)

## Testing

Run the test suite:

```powershell
mvn test
```

The repository includes controller, service, integration-health, and event-flow tests.

## Deployment Notes

- Production data now lives in the shared `moneykeeper` MySQL database.
- Shared middleware is expected for MySQL, Redis, Elasticsearch, and Kafka.
- Flyway migrations are the source of truth for schema changes.
- Export files should be stored on a persistent host volume.

Host-side deployment references are kept under [deploy](./deploy).

## Current Status

Implemented and shipped:

- Shared ledger model
- Budget foundation
- Notification center
- Export job flow
- Search and statistics
- Kafka event pipeline
- Production cutover to the main `moneykeeper` database

Still evolving:

- frontend iteration work
- stricter request normalization for some legacy fields
- further operations hardening

## License

MIT. See [LICENSE](./LICENSE).
