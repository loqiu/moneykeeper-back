#!/usr/bin/env bash
set -euo pipefail

MYSQL_CONTAINER="${MYSQL_CONTAINER:-mysql}"
DB_NAME="${DB_NAME:-moneykeeper}"
EXPECTED_BASELINE_TABLES=(
  users
  categories
  moneykeeper
  membership_plan
  payment_order
  payment_subscription
  payment_webhook_event
)
EXPECTED_TARGET_TABLES=(
  ledger
  ledger_member
  ledger_invite
  budget
  budget_rule
  export_job
  notification_log
  flyway_schema_history
)

log() {
  printf '[prod-preflight] %s\n' "$*"
}

fail() {
  printf '[prod-preflight] ERROR: %s\n' "$*" >&2
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

main() {
  require_cmd docker

  docker ps --format '{{.Names}}' | grep -Fx "$MYSQL_CONTAINER" >/dev/null 2>&1 \
    || fail "MySQL container '$MYSQL_CONTAINER' is not running"

  local root_password
  root_password="$(read_mysql_root_password)"

  local db_exists
  db_exists="$(mysql_query "$root_password" "SELECT COUNT(*) FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='${DB_NAME}';" | tr -d '\r')"
  [[ "$db_exists" == "1" ]] || fail "Database '${DB_NAME}' does not exist"

  log "Inspecting database '${DB_NAME}' in container '${MYSQL_CONTAINER}'"
  printf '\n'

  log "Current tables"
  mysql_query "$root_password" "SHOW TABLES FROM \`${DB_NAME}\`;"
  printf '\n'

  log "Baseline row counts"
  local missing_baseline=()
  local table_exists
  local row_count
  for table in "${EXPECTED_BASELINE_TABLES[@]}"; do
    table_exists="$(mysql_query "$root_password" "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='${DB_NAME}' AND TABLE_NAME='${table}';" | tr -d '\r')"
    if [[ "$table_exists" != "1" ]]; then
      missing_baseline+=("$table")
      continue
    fi

    row_count="$(mysql_query "$root_password" "SELECT COUNT(*) FROM \`${DB_NAME}\`.\`${table}\`;" | tr -d '\r')"
    printf '  %-24s %s\n' "$table" "$row_count"
  done
  printf '\n'

  if (( ${#missing_baseline[@]} > 0 )); then
    fail "Missing baseline tables: ${missing_baseline[*]}"
  fi

  local flyway_history_count
  local category_ledger_id
  local moneykeeper_ledger_id
  flyway_history_count="$(mysql_query "$root_password" "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='${DB_NAME}' AND TABLE_NAME='flyway_schema_history';" | tr -d '\r')"
  category_ledger_id="$(mysql_query "$root_password" "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='${DB_NAME}' AND TABLE_NAME='categories' AND COLUMN_NAME='ledger_id';" | tr -d '\r')"
  moneykeeper_ledger_id="$(mysql_query "$root_password" "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='${DB_NAME}' AND TABLE_NAME='moneykeeper' AND COLUMN_NAME='ledger_id';" | tr -d '\r')"

  local present_target_tables=0
  local missing_target_tables=()
  for table in "${EXPECTED_TARGET_TABLES[@]}"; do
    table_exists="$(mysql_query "$root_password" "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='${DB_NAME}' AND TABLE_NAME='${table}';" | tr -d '\r')"
    if [[ "$table_exists" == "1" ]]; then
      present_target_tables=$((present_target_tables + 1))
    else
      missing_target_tables+=("$table")
    fi
  done

  log "Cutover state"
  printf '  %-24s %s\n' "flyway_schema_history" "$flyway_history_count"
  printf '  %-24s %s\n' "categories.ledger_id" "$category_ledger_id"
  printf '  %-24s %s\n' "moneykeeper.ledger_id" "$moneykeeper_ledger_id"
  printf '  %-24s %s/%s\n' "target tables present" "$present_target_tables" "${#EXPECTED_TARGET_TABLES[@]}"
  printf '\n'

  if [[ "$flyway_history_count" == "0" && "$category_ledger_id" == "0" && "$moneykeeper_ledger_id" == "0" && "$present_target_tables" == "0" ]]; then
    log "Result: legacy-ready"
    log "This database still matches the pre-Flyway baseline and is ready for first cutover."
    return
  fi

  if [[ "$flyway_history_count" == "1" && "$category_ledger_id" == "1" && "$moneykeeper_ledger_id" == "1" && "$present_target_tables" == "${#EXPECTED_TARGET_TABLES[@]}" ]]; then
    log "Result: migrated"
    log "Flyway has already claimed this database. Installed versions:"
    mysql_query "$root_password" "SELECT version, success FROM \`${DB_NAME}\`.flyway_schema_history ORDER BY installed_rank;"
    return
  fi

  log "Result: mixed"
  if (( ${#missing_target_tables[@]} > 0 )); then
    log "Missing target tables: ${missing_target_tables[*]}"
  fi
  fail "Database '${DB_NAME}' is in a partial migration state. Stop and inspect before deploying."
}

main "$@"
