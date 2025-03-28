# Certified Builder API

[![PR Workflow](https://github.com/maxsonferovante/certified-builder-api/actions/workflows/gradle.yml/badge.svg)](https://github.com/maxsonferovante/certified-builder-api/actions/workflows/gradle.yml)

API desenvolvida em Spring Boot para gerenciamento de builders certificados, integrando com serviços AWS como SQS e S3.

## Tecnologias Utilizadas

- Java 24
- Spring Boot 3.4.4
- Spring Security (API Key Authentication)
- Spring Cloud AWS
- MongoDB
- Gradle
- Docker

## Funcionalidades

- Autenticação via API Key
- Integração com Amazon SQS para processamento assíncrono
- Armazenamento de arquivos no Amazon S3
- Persistência de dados com MongoDB
- API RESTful

## Pré-requisitos

- JDK 24
- Docker (opcional)
- Credenciais AWS configuradas
- MongoDB

## Configuração

### Variáveis de Ambiente

Configure as seguintes variáveis de ambiente:

```bash
# AWS Credentials
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_REGION=your_region

# MongoDB
MONGODB_URI=your_mongodb_uri

# API Security
API_KEY=your_api_key
```

### AWS Services

A API utiliza os seguintes serviços AWS:
- SQS para processamento de mensagens
- S3 para armazenamento de arquivos

## Executando a Aplicação

### Localmente

```bash
./gradlew bootRun
```

### Com Docker

```bash
docker build -t certified-builder-api .
docker run -p 8081:8081 certified-builder-api
```

## Documentação

### Segurança
- Autenticação via API Key: [Documentação Spring Security API Key](https://www.baeldung.com/spring-boot-api-key-secret)

### AWS Services
- SQS: [Documentação Spring Cloud AWS SQS](https://www.baeldung.com/java-spring-cloud-aws-v3-intro)
- S3: [Documentação Spring Cloud AWS S3](https://docs.awspring.io/spring-cloud-aws/docs/3.3.0/reference/html/index.html#spring-cloud-aws-s3)

## Endpoints

A API está disponível na porta 8081.

### Base URL
```
http://localhost:8081/certified
```

### Endpoints Disponíveis

#### 1. Listar Pedidos
```http
GET /certified?product_id={product_id}
```

**Descrição**: Retorna a lista de pedidos para um produto específico.

**Parâmetros**:
- `product_id` (query): ID do produto

**Resposta**: Lista de `TechOrdersResponse`

#### 2. Listar Pedidos Construídos
```http
GET /certified/builded?product_id={product_id}
```

**Descrição**: Retorna a lista de pedidos já construídos para um produto específico.

**Parâmetros**:
- `product_id` (query): ID do produto

**Resposta**: Lista de `RecoverCertificatesResponse`

#### 3. Construir Pedidos
```http
POST /certified/build
```

**Descrição**: Inicia o processo de construção de pedidos.

**Corpo da Requisição**:
```json
{
    // BuildOrdersRequest
}
```

**Resposta**: `BuildOrdersResponse`

#### 4. Deletar Produto
```http
DELETE /certified/delete/product?product_id={product_id}
```

**Descrição**: Remove um produto e seus pedidos associados.

**Parâmetros**:
- `product_id` (query): ID do produto a ser deletado

**Resposta**: `DeleteProductResponse`

### Exemplos de Uso

#### Listar Pedidos
```bash
curl -X GET "http://localhost:8081/certified?product_id=123"
```

#### Construir Pedidos
```bash
curl -X POST "http://localhost:8081/certified/build" \
     -H "Content-Type: application/json" \
     -d '{
           // BuildOrdersRequest payload
         }'
```

#### Deletar Produto
```bash
curl -X DELETE "http://localhost:8081/certified/delete/product?product_id=123"
```

## Contribuindo

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.