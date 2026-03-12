# Platform Deployment

This folder contains the physical-host deployment files for the `codex/platform` backend image.

## Branch to tag mapping

- `main` -> `rochelle98/moneykeeper-back:latest`
- `test` -> `rochelle98/moneykeeper-back:test`
- `codex/platform` -> `rochelle98/moneykeeper-back:platform`

## Files

- `docker-compose.platform.yml`: platform app container wired to the host's existing middleware containers
- `platform.env.example`: example environment variables for the host

## Shared middleware model

The platform deployment reuses the host's existing `mysql`, `redis`, and `elasticsearch` containers.
It stays logically isolated by using:

- MySQL database: `moneykeeper_platform`
- Redis database: `1`
- Elasticsearch index: `moneykeeper-records-platform`
- Export job files: `./data/moneykeeper-platform/export-jobs` mounted into the app container

When `main` is released, the same Flyway migrations can run against the shared production MySQL database so all final tables and data live in the main middleware stack.

## Host prerequisites

1. Install Docker Engine and Docker Compose.
2. Make sure the existing middleware containers are reachable on the shared Docker network.
3. Copy `platform.env.example` to `platform.env` and replace placeholder secrets.
4. Create the platform database and app user inside the shared MySQL container before the first start.
5. Make sure the export-job storage directory is writable on the host. The compose file mounts it automatically under `./data/moneykeeper-platform/export-jobs`.

Example SQL:

```sql
CREATE DATABASE IF NOT EXISTS moneykeeper_platform
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'moneykeeper'@'%' IDENTIFIED BY 'change-me-db-password';
GRANT ALL PRIVILEGES ON moneykeeper_platform.* TO 'moneykeeper'@'%';
FLUSH PRIVILEGES;
```

## Start the stack

```bash
docker compose --env-file deploy/platform.env -f deploy/docker-compose.platform.yml pull
docker compose --env-file deploy/platform.env -f deploy/docker-compose.platform.yml up -d
```

## Validate services

```bash
docker compose --env-file deploy/platform.env -f deploy/docker-compose.platform.yml ps
curl http://127.0.0.1:8082/actuator/health
curl http://127.0.0.1:9200
```

## Rebuild Elasticsearch records after deploy

For a fresh environment, new writes will sync automatically. For existing historical data, call one of these admin endpoints after the app is healthy:

```bash
curl -X POST "http://127.0.0.1:8082/api/search/records/reindex"
curl -X POST "http://127.0.0.1:8082/api/search/records/reindex/ledger?ledgerId=31"
```

## Export jobs

New export jobs are now asynchronous:

- `POST /api/ledgers/{ledgerId}/export-jobs` queues the task as `pending`
- the scheduler writes the workbook into the mounted export-job directory
- the requester receives an `info` notification when the job is ready, or an `error` notification if generation fails

If you change `MONEYKEEPER_EXPORT_JOB_STORAGE_DIR`, update both the environment variable and the bind mount in `docker-compose.platform.yml` so completed jobs remain downloadable after container restarts.

## Optional Kafka record events

Record create/update/delete can also flow through Kafka:

- keep `MONEYKEEPER_KAFKA_ENABLED=false` to run record search sync and budget threshold recalculation in-process
- set `MONEYKEEPER_KAFKA_ENABLED=true` and `MONEYKEEPER_KAFKA_LISTENER_AUTO_STARTUP=true` to publish record events to Kafka
- use `MONEYKEEPER_KAFKA_RECORD_EVENT_TOPIC` and `MONEYKEEPER_KAFKA_EXPORT_JOB_TOPIC` to override topic names if needed

When Kafka mode is enabled:

- ledger record search refresh and budget warning notifications are eventually consistent instead of strictly in-request synchronous
- export jobs can be kicked off by Kafka immediately after creation, while the scheduler still remains as a safety-net fallback for pending jobs
