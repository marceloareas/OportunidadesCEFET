#!/usr/bin/env sh
# Restaura um dump gerado por backup-db.sh. ATENÇÃO: substitui (--drop) os dados atuais.
# As credenciais vêm do .env, as mesmas usadas pelo docker compose.
set -eu

cd "$(dirname "$0")"

if [ "$#" -ne 1 ]; then
  echo "Uso: ./restore-db.sh backups/<arquivo-de-backup>"
  exit 1
fi

BACKUP_FILE="$1"

if [ ! -f "$BACKUP_FILE" ]; then
  echo "Arquivo de backup nao encontrado: $BACKUP_FILE"
  exit 1
fi

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

docker compose up -d "$CONTAINER_SERVICE"

docker compose exec -T "$CONTAINER_SERVICE" \
  mongorestore \
  --uri="$DB_URI" \
  --archive \
  --gzip \
  --drop < "$BACKUP_FILE"

echo "Banco restaurado a partir de: $BACKUP_FILE"
