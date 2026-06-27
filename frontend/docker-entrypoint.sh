#!/bin/sh
set -e

cat > /app/public/env.js <<EOF
window.__APP_ENV__ = {
  apiBaseUrl: "${FRONTEND_API_BASE_URL:-/api}"
};
EOF

export FRONTEND_PROXY_TARGET="${FRONTEND_PROXY_TARGET:-http://backend:8080}"

exec npm run start -- --host 0.0.0.0 --port 4200 --poll 2000 --allowed-hosts
