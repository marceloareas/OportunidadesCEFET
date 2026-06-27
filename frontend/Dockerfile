FROM node:20-alpine

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 4200

ENV CHOKIDAR_USEPOLLING=true

ENTRYPOINT ["/docker-entrypoint.sh"]
