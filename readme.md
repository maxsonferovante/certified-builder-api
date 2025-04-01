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
http://localhost:8081/api/v1/certified
```

### Endpoints Disponíveis

#### 1. Criar Ordem de Construção de Certificados
```http
POST /certified/build-orders
```

**Descrição**: Inicia o processo de construção de certificados para um produto específico.

**Headers**:
- `Content-Type: application/json`
- `X-API-KEY: {api_key}`

**Corpo da Requisição**:
```json
{
    "productId": "316"
}
```

**Resposta**:
```json
{
    "certificateQuantity": 2,
    "existingOrders": [],
    "newOrders": [452, 317]
}
```

#### 2. Monitorar Progresso
```http
GET /certified/statistics?productId={product_id}
```

**Descrição**: Retorna estatísticas sobre o progresso da geração de certificados.

**Headers**:
- `X-API-KEY: {api_key}`

**Parâmetros**:
- `product_id` (query): ID do produto

**Resposta**:
```json
{
    "productId": 316,
    "productName": "Evento de Teste",
    "totalCertificates": 2,
    "successfulCertificates": 2,
    "failedCertificates": 0,
    "pendingCertificates": 0
}
```

#### 3. Recuperar Certificados
```http
GET /certified/recover-certificates?productId={product_id}
```

**Descrição**: Retorna a lista de certificados gerados para um produto específico.

**Headers**:
- `X-API-KEY: {api_key}`

**Parâmetros**:
- `product_id` (query): ID do produto

**Resposta**:
```json
[
    {
        "success": true,
        "certificateId": "67ec239c960dac7f218f4427",
        "certificateUrl": "https://example-bucket.s3.amazonaws.com/certificates/316/452/certificate.png",
        "generetedDate": "2025-04-01T17:34:20.748",
        "productId": 316,
        "productName": "Evento de Teste",
        "orderId": 452,
        "orderDate": "2025-03-26T20:55:25"
    }
]
```

### Exemplos de Uso

#### Criar Ordem de Certificados
```bash
curl --request POST \
  --url 'http://localhost:8081/api/v1/certified/build-orders' \
  --header 'Content-Type: application/json' \
  --header 'X-API-KEY: example-api-key-123' \
  --data '{
    "productId": "316"
  }'
```

#### Monitorar Progresso
```bash
curl --request GET \
  --url 'http://localhost:8081/api/v1/certified/statistics?productId=316' \
  --header 'X-API-KEY: example-api-key-123'
```

#### Recuperar Certificados
```bash
curl --request GET \
  --url 'http://localhost:8081/api/v1/certified/recover-certificates?productId=316' \
  --header 'X-API-KEY: example-api-key-123'
```

### Notas Importantes

- Todos os endpoints requerem autenticação via API Key no header `X-API-KEY`
- Recomenda-se implementar polling no endpoint de estatísticas até que `successfulCertificates` seja igual a `totalCertificates`
- As URLs dos certificados são temporárias e expiram após 24 horas

## Contribuindo

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.