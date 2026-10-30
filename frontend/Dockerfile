# Imagem de produção: compila o Angular e entrega os arquivos estáticos pelo nginx.
# Para desenvolvimento com ng serve e hot reload, veja Dockerfile.dev e docker-compose.dev.yml.

# 1) Build do Angular
FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npx ng build --configuration production

# 2) nginx servindo o app e repassando /api ao backend
FROM nginx:1.27-alpine

ENV BACKEND_UPSTREAM=http://backend:8080

COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80
