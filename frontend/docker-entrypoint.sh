#!/bin/sh
set -e

cat > /app/public/env.js <<EOF
window.__APP_ENV__ = {
  apiBaseUrl: "${FRONTEND_API_BASE_URL:-http://localhost:8080}"
};
EOF

exec npm run start -- --host 0.0.0.0 --port 4200 --poll 2000