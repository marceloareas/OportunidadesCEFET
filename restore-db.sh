#!/usr/bin/env sh
set -eu

DB_URI="mongodb://root:secret@localhost:27017/oportunidades_cefet?authSource=admin"
CONTAINER_SERVICE="mongo"

if [ "$#" -ne 1 ]; then
  echo "Uso: ./restore-db.sh <arquivo-de-backup>"
  exit 1
fi

BACKUP_FILE="$1"

if [ ! -f "$BACKUP_FILE" ]; then
  echo "Arquivo de backup nao encontrado: $BACKUP_FILE"
  exit 1
fi

docker compose up -d "$CONTAINER_SERVICE"

docker compose exec -T "$CONTAINER_SERVICE" \
  mongorestore \
  --uri="$DB_URI" \
  --archive \
  --gzip \
  --drop < "$BACKUP_FILE"

echo "Banco restaurado a partir de: $BACKUP_FILE"
