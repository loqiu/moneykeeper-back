# Production Cutover Runbook

This runbook is for promoting the `codex/platform` backend changes into the shared production middleware stack while keeping the final production data in the existing `moneykeeper` MySQL database.

## Target state

- MySQL container stays shared: `mysql`
- Production database stays shared: `moneykeeper`
- Redis container stays shared: `redis`
- Elasticsearch container stays shared: `elasticsearch`
- Flyway owns schema evolution on `moneykeeper`
- `moneykeeper_platform` remains a platform/pre-release database only

## What changes in the first production cutover

When the new backend image starts against `moneykeeper` for the first time, Flyway will:

1. baseline the existing legacy schema at version `1`
2. run `V2__ledger_foundation.sql`
3. run `V3__budget_foundation.sql`
4. run `V4__export_job_notification_log.sql`
5. run `V5__export_job_async_execution.sql`

That means production keeps its current data in place and gains:

- new tables: `ledger`, `ledger_member`, `ledger_invite`, `budget`, `budget_rule`, `export_job`, `notification_log`, `flyway_schema_history`
- new columns: `categories.ledger_id`, `moneykeeper.ledger_id`
- backfilled personal ledgers for existing users
- backfilled `ledger_id` values for existing categories and records

## Before the release window

1. Merge `codex/platform` to `main` so GitHub Actions publishes the `latest` image.
2. Confirm the new image exists on Docker Hub.
3. Run the host preflight:

```bash
bash deploy/prod-main-preflight.sh
```

Expected result before first release: `Result: legacy-ready`

4. Rehearse the migration on a cloned copy of `moneykeeper`:

```bash
bash deploy/rehearse-main-db-migration.sh
```

Expected result:

- rehearsal app health is `UP`
- `flyway_schema_history` shows versions `1` to `5`
- `categories.ledger_id` and `moneykeeper.ledger_id` exist in the rehearsal database

## Production compose requirements

The production `moneykeeper-backend` service should explicitly set these environment variables when the new image is rolled out:

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=prod
  - MONEYKEEPER_DB_URL=jdbc:mysql://mysql:3306/moneykeeper?useUnicode=true&characterEncoding=utf8&allowPublicKeyRetrieval=true&serverTimezone=UTC
  - MONEYKEEPER_DB_USERNAME=<production-app-user>
  - MONEYKEEPER_DB_PASSWORD=<production-app-password>
  - MONEYKEEPER_REDIS_HOST=redis
  - MONEYKEEPER_REDIS_PORT=6379
  - MONEYKEEPER_REDIS_DATABASE=0
  - MONEYKEEPER_ELASTICSEARCH_ENABLED=true
  - MONEYKEEPER_ELASTICSEARCH_HOST=elasticsearch
  - MONEYKEEPER_ELASTICSEARCH_PORT=9200
  - MONEYKEEPER_ELASTICSEARCH_INDEX_NAME=moneykeeper-records
  - MONEYKEEPER_KAFKA_ENABLED=true
  - MONEYKEEPER_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
  - MONEYKEEPER_KAFKA_LISTENER_AUTO_STARTUP=true
  - MONEYKEEPER_KAFKA_RECORD_EVENT_TOPIC=moneykeeper-record-events
  - MONEYKEEPER_KAFKA_EXPORT_JOB_TOPIC=moneykeeper-export-job-events
  - MONEYKEEPER_NACOS_DISCOVERY_ENABLED=false
  - MONEYKEEPER_NACOS_CONFIG_ENABLED=false
  - MONEYKEEPER_DUBBO_ENABLED=false
  - MONEYKEEPER_PAYMENT_ENABLED=true
  - DUBBO_REGISTRY_ADDRESS=N/A
  - DUBBO_REGISTRY_REGISTER=false
  - DUBBO_CONFIG_CENTER_ADDRESS=N/A
```

If production still uses legacy `APP_PAYMENT_*` variables, keep them only if the running image still requires them. The new backend code reads `MONEYKEEPER_PAYMENT_ENABLED` for payment feature toggling.

## Release window steps

1. Freeze writes to the production app or put the frontend into maintenance mode.
2. Make a fresh backup of `moneykeeper`.
3. Pull the new backend image on the host.
4. Update the production `moneykeeper-backend` service env to the explicit variables above.
5. Recreate only the backend container.
6. Wait for `http://127.0.0.1:8081/actuator/health` to return `UP`.
7. Run the preflight script again against `moneykeeper`.

Expected result after release: `Result: migrated`

## Post-release checks

1. Confirm row counts in `users`, `categories`, and `moneykeeper` did not drop.
2. Confirm `ledger`, `budget`, `export_job`, and `notification_log` now exist in `moneykeeper`.
3. Log in with an existing production user and confirm a default personal ledger is present.
4. Create a test record and verify:
   - it is searchable
   - budget recalculation still works
   - no unexpected error notifications appear
5. If historical search data is incomplete, run one of these admin endpoints:

```bash
curl -X POST "http://127.0.0.1:8081/api/search/records/reindex"
curl -X POST "http://127.0.0.1:8081/api/search/records/reindex/ledger?ledgerId=<ledgerId>"
```

## Rollback guidance

- If the container fails before Flyway changes anything, restore the old backend image and restart the container.
- If Flyway has already migrated `moneykeeper`, do not try to “undo” the schema manually in place during the outage.
- Restore the `moneykeeper` backup into a separate recovery database first, compare it with the migrated database, then decide whether to cut traffic back to the previous image or restore from backup.
- Keep `moneykeeper_platform` untouched during production rollback decisions. It is not the production source of truth.
