#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DB_NAME="${MYSQL_DATABASE:-easyweibo}"
DB_USER="${MYSQL_USER:-easyweibo}"
DB_PASSWORD="${MYSQL_PASSWORD:-123456}"

mysql -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$ROOT_DIR/src/main/resources/schema.sql"
mysql -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$ROOT_DIR/src/main/resources/data.sql"

echo "Reset database '$DB_NAME' with schema.sql + data.sql"
