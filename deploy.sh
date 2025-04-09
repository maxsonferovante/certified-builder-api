#!/bin/bash

set -e  # para o script se qualquer comando falhar

echo "🔐 Criando acme.json para o Traefik..."
mkdir -p ./letsencrypt
touch ./letsencrypt/acme.json
chmod 600 ./letsencrypt/acme.json

echo "🌍 Exportando variáveis de ambiente..."
export MONGO_URI="${MONGO_URI}"
export MONGO_INITDB_ROOT_USERNAME="${MONGO_INITDB_ROOT_USERNAME}"
export MONGO_INITDB_ROOT_PASSWORD="${MONGO_INITDB_ROOT_PASSWORD}"
export URL_SERVICE_TECH="${URL_SERVICE_TECH}"
export API_KEY="${API_KEY}"
export AWS_ACCESS_KEY="${AWS_ACCESS_KEY}"
export AWS_SECRET_KEY="${AWS_SECRET_KEY}"
export AWS_REGION="${AWS_REGION}"
export QUEUE_NAME_NOTIFICATION_GENERATION="${QUEUE_NAME_NOTIFICATION_GENERATION}"
export QUEUE_NAME_BUILDER="${QUEUE_NAME_BUILDER}"
export S3_BUCKET_NAME="${S3_BUCKET_NAME}"

echo "🚀 Subindo containers com Docker Compose..."
docker compose -f docker-compose.prod.yml up -d --build
