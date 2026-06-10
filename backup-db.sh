#!/usr/bin/env sh
set -eu

DB_URI="mongodb://root:secret@localhost:27017/oportunidades_cefet?authSource=admin"
CONTAINER_SERVICE="mongo"
BACKUP_FILE="backup-oportunidades-$(date +%Y-%m-%d)"

docker compose up -d "$CONTAINER_SERVICE"

docker compose exec -T "$CONTAINER_SERVICE" \
  mongodump \
  --uri="$DB_URI" \
  --archive \
  --gzip > "$BACKUP_FILE"

echo "Backup gerado: $BACKUP_FILE"
