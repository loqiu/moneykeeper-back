# Platform Deployment

This folder contains the physical-host deployment files for the `codex/platform` backend image.

## Branch to tag mapping

- `main` -> `rochelle98/moneykeeper-back:latest`
- `test` -> `rochelle98/moneykeeper-back:test`
- `codex/platform` -> `rochelle98/moneykeeper-back:platform`

## Files

- `docker-compose.platform.yml`: isolated stack for the platform test environment
- `platform.env.example`: example environment variables for the host

## Host prerequisites

1. Install Docker Engine and Docker Compose.
2. Set `vm.max_map_count=262144` on the host for Elasticsearch.
3. Copy `platform.env.example` to `platform.env` and replace placeholder secrets.

## Default host ports

- App: `8082`
- MySQL: `3307`
- Redis: `6380`
- Elasticsearch: `9201`

These defaults intentionally avoid the existing production stack ports on the same host.

## Start the stack

```bash
docker compose --env-file deploy/platform.env -f deploy/docker-compose.platform.yml pull
docker compose --env-file deploy/platform.env -f deploy/docker-compose.platform.yml up -d
```

## Validate services

```bash
docker compose --env-file deploy/platform.env -f deploy/docker-compose.platform.yml ps
curl http://127.0.0.1:9201
curl http://127.0.0.1:8082/actuator/health
```

## Rebuild Elasticsearch records after deploy

After the app is healthy, call one of these admin endpoints:

```bash
curl -X POST "http://127.0.0.1:8082/api/search/records/reindex"
curl -X POST "http://127.0.0.1:8082/api/search/records/reindex/ledger?ledgerId=31"
```