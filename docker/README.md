# Infraestrutura Local com LocalStack

## Visão Geral

Esta infraestrutura usa o **LocalStack** para simular todos os serviços AWS necessários:
- **DynamoDB**: Banco de dados NoSQL
- **S3**: Armazenamento de arquivos  
- **SQS**: Filas de mensagens

## Serviços Disponíveis

### LocalStack (Porta 4566)
- **Endpoint**: `http://localhost:4566`
- **Credenciais**: `test/test`
- **Região**: `us-west-2`

### DynamoDB Admin (Porta 8001)
- **URL**: `http://localhost:8001`
- Interface web para gerenciar tabelas DynamoDB

## Estrutura de Tabelas DynamoDB

### 1. certificates
- **Hash Key**: `id` (String)
- **GSI**: `OrderIdIndex`, `ProductIdIndex`

### 2. products  
- **Hash Key**: `id` (String)
- **GSI**: `ProductIdIndex`

### 3. participants
- **Hash Key**: `id` (String) 
- **GSI**: `EmailIndex`

### 4. orders
- **Hash Key**: `id` (String)
- **GSI**: `OrderIdIndex`

## Como Usar

### Iniciar Infraestrutura
```bash
docker-compose up -d
```

### Verificar Status
```bash
# Verificar se LocalStack está rodando
docker-compose ps

# Listar tabelas DynamoDB
docker exec -it localstack_main awslocal dynamodb list-tables --region us-west-2

# Verificar filas SQS
docker exec -it localstack_main awslocal sqs list-queues --region us-west-2

# Verificar buckets S3
docker exec -it localstack_main awslocal s3 ls
```

### Parar Infraestrutura
```bash
docker-compose down
```

## Comandos Úteis

### DynamoDB
```bash
# Descrever uma tabela
awslocal dynamodb describe-table --table-name certificates --region us-west-2

# Escanear registros de uma tabela
awslocal dynamodb scan --table-name certificates --region us-west-2
```

### SQS
```bash
# Enviar mensagem para fila
awslocal sqs send-message --queue-url http://localhost:4566/000000000000/builder.fifo --message-body "Test message" --region us-west-2
```

### S3
```bash
# Listar objetos do bucket
awslocal s3 ls s3://maal-upload-dev
```

## Configuração da Aplicação

A aplicação está configurada para usar os endpoints do LocalStack:

```properties
# DynamoDB
amazon.dynamodb.endpoint=http://localhost:4566

# AWS Geral  
spring.cloud.aws.endpoint=http://localhost:4566
spring.cloud.aws.credentials.accessKey=test
spring.cloud.aws.credentials.secretKey=test
spring.cloud.aws.region.static=us-west-2

# S3
spring.cloud.aws.s3.endpoint=http://localhost:4566
spring.cloud.aws.s3.path-style-access=true
``` 