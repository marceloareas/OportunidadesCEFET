#!/usr/bin/env sh
# Gera um dump compactado do MongoDB em backups/ (pasta fora do git).
# As credenciais vêm do .env, as mesmas usadas pelo docker compose.
set -eu

cd "$(dirname "$0")"

if [ ! -f .env ]; then
  echo "Arquivo .env nao encontrado. Copie o .env.example para .env e preencha."
  exit 1
fi

set -a
. ./.env
set +a

DB_NAME="${MONGO_INITDB_DATABASE:-oportunidades_cefet}"
DB_URI="mongodb://${MONGO_INITDB_ROOT_USERNAME}:${MONGO_INITDB_ROOT_PASSWORD}@localhost:27017/${DB_NAME}?authSource=admin"
CONTAINER_SERVICE="mongo"

mkdir -p backups
BACKUP_FILE="backups/backup-oportunidades-$(date +%Y-%m-%d_%H%M).gz"

docker compose up -d "$CONTAINER_SERVICE"

docker compose exec -T "$CONTAINER_SERVICE" \
  mongodump \
  --uri="$DB_URI" \
  --archive \
  --gzip > "$BACKUP_FILE"

echo "Backup gerado: $BACKUP_FILE"
