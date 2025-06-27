    #!/bin/sh

echo "Init LocalStack - Criando recursos AWS"

# Criação das filas SQS
echo "Criando filas SQS..."
awslocal sqs create-queue --queue-name builder.fifo --attributes FifoQueue=true
awslocal sqs create-queue --queue-name notification_generation.fifo --attributes FifoQueue=true

# Criação do bucket S3
echo "Criando bucket S3..."
awslocal s3api create-bucket --bucket maal-upload-dev --region us-west-2 --create-bucket-configuration LocationConstraint=us-west-2

# Criação das tabelas DynamoDB
echo "Criando tabelas DynamoDB..."

# Tabela certificates
echo "Criando tabela certificates..."
awslocal dynamodb create-table --cli-input-json file:///docker/schemas/certificates_schema.json

# Tabela products
echo "Criando tabela products..."
awslocal dynamodb create-table --cli-input-json file:///docker/schemas/products_schema.json

# Tabela participants
echo "Criando tabela participants..."
awslocal dynamodb create-table --cli-input-json file:///docker/schemas/participants_schema.json

# Tabela orders
echo "Criando tabela orders..."
awslocal dynamodb create-table --cli-input-json file:///docker/schemas/orders_schema.json

echo "Inicialização concluída com sucesso!"