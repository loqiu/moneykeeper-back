#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEFAULT_ENV_FILE="${SCRIPT_DIR}/platform.env"

if [[ -f "${ENV_FILE:-$DEFAULT_ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE:-$DEFAULT_ENV_FILE}"
  set +a
fi

TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
MYSQL_CONTAINER="${MYSQL_CONTAINER:-mysql}"
SOURCE_DB_NAME="${SOURCE_DB_NAME:-moneykeeper}"
REHEARSAL_DB_NAME="${REHEARSAL_DB_NAME:-moneykeeper_release_rehearsal_${TIMESTAMP}}"
REHEARSAL_CONTAINER_NAME="${REHEARSAL_CONTAINER_NAME:-moneykeeper-main-rehearsal}"
REHEARSAL_PORT="${REHEARSAL_PORT:-18082}"
SHARED_NETWORK="${SHARED_NETWORK:-${MONEYKEEPER_SHARED_NETWORK:-env_default}}"
APP_IMAGE="${APP_IMAGE:-rochelle98/moneykeeper-back:${MONEYKEEPER_IMAGE_TAG:-platform}}"
APP_DB_USERNAME="${APP_DB_USERNAME:-${MONEYKEEPER_DB_USERNAME:-moneykeeper}}"
APP_DB_PASSWORD="${APP_DB_PASSWORD:-${MONEYKEEPER_DB_PASSWORD:-}}"
REDIS_HOST="${REDIS_HOST:-${MONEYKEEPER_REDIS_HOST:-redis}}"
REDIS_PORT="${REDIS_PORT:-${MONEYKEEPER_REDIS_PORT:-6379}}"
REDIS_PASSWORD="${REDIS_PASSWORD:-${MONEYKEEPER_REDIS_PASSWORD:-}}"
REDIS_DATABASE="${REDIS_DATABASE:-15}"
ELASTICSEARCH_HOST="${ELASTICSEARCH_HOST:-${MONEYKEEPER_ELASTICSEARCH_HOST:-elasticsearch}}"
ELASTICSEARCH_PORT="${ELASTICSEARCH_PORT:-${MONEYKEEPER_ELASTICSEARCH_PORT:-9200}}"
ELASTICSEARCH_INDEX_NAME="${ELASTICSEARCH_INDEX_NAME:-moneykeeper-records-rehearsal-${TIMESTAMP}}"
JWT_SECRET="${JWT_SECRET:-moneykeeper_release_rehearsal_jwt_secret_${TIMESTAMP}}"
EXPORT_JOB_STORAGE_DIR="${EXPORT_JOB_STORAGE_DIR:-/data/export-jobs}"
HOST_EXPORT_JOB_DIR="${HOST_EXPORT_JOB_DIR:-${SCRIPT_DIR}/rehearsal-data/export-jobs}"
BACKUP_DIR="${BACKUP_DIR:-${SCRIPT_DIR}/rehearsal-data/backups}"
WAIT_SECONDS="${WAIT_SECONDS:-180}"
KEEP_REHEARSAL="${KEEP_REHEARSAL:-false}"

log() {
  printf '[rehearsal] %s\n' "$*"
}

fail() {
  printf '[rehearsal] ERROR: %s\n' "$*" >&2
  exit 1
}

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || fail "Missing required command: $1"
}

read_mysql_root_password() {
  if [[ -n "${MYSQL_ROOT_PASSWORD:-}" ]]; then
    printf '%s' "$MYSQL_ROOT_PASSWORD"
    return
  fi

  local detected
  detected="$(docker exec "$MYSQL_CONTAINER" sh -lc 'printenv MYSQL_ROOT_PASSWORD' 2>/dev/null || true)"
  [[ -n "$detected" ]] || fail "MYSQL_ROOT_PASSWORD is not set and could not be read from container '$MYSQL_CONTAINER'"
  printf '%s' "$detected"
}

mysql_exec() {
  local root_password="$1"
  shift
  docker exec -e MYSQL_PWD="$root_password" -i "$MYSQL_CONTAINER" mysql -uroot -N "$@"
}

mysql_query() {
  local root_password="$1"
  local sql="$2"
  printf '%s\n' "$sql" | mysql_exec "$root_password"
}

cleanup() {
  if [[ "$KEEP_REHEARSAL" == "true" ]]; then
    log "Keeping rehearsal container, database, and Elasticsearch index for inspection."
    return
  fi

  log "Cleaning up rehearsal resources"
  docker rm -f "$REHEARSAL_CONTAINER_NAME" >/dev/null 2>&1 || true
  if [[ -n "${MYSQL_ROOT_PASSWORD_RUNTIME:-}" ]]; then
    mysql_query "$MYSQL_ROOT_PASSWORD_RUNTIME" "DROP DATABASE IF EXISTS \`${REHEARSAL_DB_NAME}\`;" >/dev/null 2>&1 || true
  fi
  curl -fsS -X DELETE "http://127.0.0.1:${ELASTICSEARCH_PORT}/${ELASTICSEARCH_INDEX_NAME}" >/dev/null 2>&1 || true
}

trap cleanup EXIT

main() {
  require_cmd docker
  require_cmd curl

  [[ -n "$APP_DB_PASSWORD" ]] || fail "APP_DB_PASSWORD is required. Set it directly or provide platform.env with MONEYKEEPER_DB_PASSWORD."

  docker ps --format '{{.Names}}' | grep -Fx "$MYSQL_CONTAINER" >/dev/null 2>&1 \
    || fail "MySQL container '$MYSQL_CONTAINER' is not running"

  docker network inspect "$SHARED_NETWORK" >/dev/null 2>&1 \
    || fail "Docker network '$SHARED_NETWORK' does not exist"

  MYSQL_ROOT_PASSWORD_RUNTIME="$(read_mysql_root_password)"

  local source_exists
  source_exists="$(mysql_query "$MYSQL_ROOT_PASSWORD_RUNTIME" "SELECT COUNT(*) FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='${SOURCE_DB_NAME}';" | tr -d '\r')"
  [[ "$source_exists" == "1" ]] || fail "Source database '${SOURCE_DB_NAME}' does not exist"

  mkdir -p "$BACKUP_DIR" "$HOST_EXPORT_JOB_DIR"
  local backup_file="${BACKUP_DIR}/${SOURCE_DB_NAME}.${TIMESTAMP}.sql"

  log "Creating backup dump at ${backup_file}"
  docker exec -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD_RUNTIME" "$MYSQL_CONTAINER" mysqldump -uroot "$SOURCE_DB_NAME" > "$backup_file"

  log "Recreating rehearsal database '${REHEARSAL_DB_NAME}'"
  mysql_query "$MYSQL_ROOT_PASSWORD_RUNTIME" "
DROP DATABASE IF EXISTS \`${REHEARSAL_DB_NAME}\`;
CREATE DATABASE \`${REHEARSAL_DB_NAME}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '${APP_DB_USERNAME}'@'%' IDENTIFIED BY '${APP_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON \`${REHEARSAL_DB_NAME}\`.* TO '${APP_DB_USERNAME}'@'%';
FLUSH PRIVILEGES;
" >/dev/null

  log "Importing source database into rehearsal database"
  cat "$backup_file" | docker exec -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD_RUNTIME" -i "$MYSQL_CONTAINER" mysql -uroot "$REHEARSAL_DB_NAME"

  docker rm -f "$REHEARSAL_CONTAINER_NAME" >/dev/null 2>&1 || true

  log "Starting rehearsal container '${REHEARSAL_CONTAINER_NAME}' on port ${REHEARSAL_PORT}"
  docker run -d \
    --name "$REHEARSAL_CONTAINER_NAME" \
    --network "$SHARED_NETWORK" \
    -p "${REHEARSAL_PORT}:8081" \
    -v "${HOST_EXPORT_JOB_DIR}:${EXPORT_JOB_STORAGE_DIR}" \
    -e SPRING_PROFILES_ACTIVE=prod \
    -e SERVER_PORT=8081 \
    -e MONEYKEEPER_DB_URL="jdbc:mysql://${MYSQL_CONTAINER}:3306/${REHEARSAL_DB_NAME}?useUnicode=true&characterEncoding=utf8&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
    -e MONEYKEEPER_DB_USERNAME="${APP_DB_USERNAME}" \
    -e MONEYKEEPER_DB_PASSWORD="${APP_DB_PASSWORD}" \
    -e MONEYKEEPER_REDIS_HOST="${REDIS_HOST}" \
    -e MONEYKEEPER_REDIS_PORT="${REDIS_PORT}" \
    -e MONEYKEEPER_REDIS_PASSWORD="${REDIS_PASSWORD}" \
    -e MONEYKEEPER_REDIS_DATABASE="${REDIS_DATABASE}" \
    -e MONEYKEEPER_ELASTICSEARCH_ENABLED=true \
    -e MONEYKEEPER_ELASTICSEARCH_HOST="${ELASTICSEARCH_HOST}" \
    -e MONEYKEEPER_ELASTICSEARCH_PORT="${ELASTICSEARCH_PORT}" \
    -e SPRING_ELASTICSEARCH_URIS="http://${ELASTICSEARCH_HOST}:${ELASTICSEARCH_PORT}" \
    -e MONEYKEEPER_ELASTICSEARCH_INDEX_NAME="${ELASTICSEARCH_INDEX_NAME}" \
    -e MONEYKEEPER_EXPORT_JOB_STORAGE_DIR="${EXPORT_JOB_STORAGE_DIR}" \
    -e MONEYKEEPER_KAFKA_ENABLED=false \
    -e MONEYKEEPER_KAFKA_LISTENER_AUTO_STARTUP=false \
    -e MONEYKEEPER_NACOS_DISCOVERY_ENABLED=false \
    -e MONEYKEEPER_NACOS_CONFIG_ENABLED=false \
    -e MONEYKEEPER_DUBBO_ENABLED=false \
    -e MONEYKEEPER_PAYMENT_ENABLED=false \
    -e SPRING_CLOUD_NACOS_DISCOVERY_ENABLED=false \
    -e SPRING_CLOUD_NACOS_CONFIG_ENABLED=false \
    -e DUBBO_REGISTRY_ADDRESS=N/A \
    -e DUBBO_REGISTRY_REGISTER=false \
    -e DUBBO_CONFIG_CENTER_ADDRESS=N/A \
    -e MONEYKEEPER_JWT_SECRET="${JWT_SECRET}" \
    "$APP_IMAGE" >/dev/null

  log "Waiting for rehearsal health endpoint"
  local elapsed=0
  until curl -fsS "http://127.0.0.1:${REHEARSAL_PORT}/actuator/health" >/dev/null 2>&1; do
    sleep 5
    elapsed=$((elapsed + 5))
    if (( elapsed >= WAIT_SECONDS )); then
      docker logs --tail 200 "$REHEARSAL_CONTAINER_NAME" >&2 || true
      fail "Rehearsal container did not become healthy within ${WAIT_SECONDS} seconds"
    fi
  done

  log "Validating Flyway results in rehearsal database"
  local status_output
  status_output="$(mysql_query "$MYSQL_ROOT_PASSWORD_RUNTIME" "
SELECT version, success FROM \`${REHEARSAL_DB_NAME}\`.flyway_schema_history ORDER BY installed_rank;
SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='${REHEARSAL_DB_NAME}' AND TABLE_NAME='categories' AND COLUMN_NAME='ledger_id';
SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='${REHEARSAL_DB_NAME}' AND TABLE_NAME='moneykeeper' AND COLUMN_NAME='ledger_id';
SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='${REHEARSAL_DB_NAME}' AND TABLE_NAME='ledger';
SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='${REHEARSAL_DB_NAME}' AND TABLE_NAME='budget';
SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='${REHEARSAL_DB_NAME}' AND TABLE_NAME='export_job';
SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='${REHEARSAL_DB_NAME}' AND TABLE_NAME='notification_log';
")"
  printf '%s\n' "$status_output"

  log "Rehearsal completed successfully"
  log "Backup dump: ${backup_file}"
  log "Rehearsal database: ${REHEARSAL_DB_NAME}"
  log "Elasticsearch index: ${ELASTICSEARCH_INDEX_NAME}"
}

main "$@"
