#!/usr/bin/env bash
set -euo pipefail

DB_USER="${QUIZ_DB_USER:-root}"
DB_PASS="${QUIZ_DB_PASS:-}"
DB_URL="${QUIZ_DB_URL:-jdbc:mysql://localhost:3306/survey?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}"

export QUIZ_DB_USER="$DB_USER"
export QUIZ_DB_PASS="$DB_PASS"
export QUIZ_DB_URL="$DB_URL"

echo "[1/3] Checking Java tools..."
if ! command -v javac >/dev/null 2>&1; then
  echo "javac not found. Install JDK 8+ and add it to PATH." >&2
  exit 1
fi

echo "[2/3] Applying database schema and seed data (if mysql is available)..."
if command -v mysql >/dev/null 2>&1; then
  if [ -n "$DB_PASS" ]; then
    MYSQL_PWD="$DB_PASS" mysql -u "$DB_USER" < database/schema_seed.sql
  else
    mysql -u "$DB_USER" < database/schema_seed.sql
  fi
  echo "Database setup complete."
else
  echo "mysql client not found. Run database/schema_seed.sql manually in MySQL." >&2
fi

echo "[3/3] Compiling Java sources..."
mkdir -p out
javac -d out src/*.java

echo
echo "Setup complete."
echo "Use this command to run (adjust connector JAR path):"
echo "java -cp \".:out:/path/to/mysql-connector-j-8.x.x.jar\" runner"
