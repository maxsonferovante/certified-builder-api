# Certified Builder API

[![Fluxo de Pull Request](https://github.com/maxsonferovante/certified-builder-api/actions/workflows/gradle.yml/badge.svg)](https://github.com/maxsonferovante/certified-builder-api/actions/workflows/gradle.yml)  
[![Gerenciamento de Tags e Releases](https://github.com/maxsonferovante/certified-builder-api/actions/workflows/release.yml/badge.svg)](https://github.com/maxsonferovante/certified-builder-api/actions/workflows/release.yml)

Certified Builder API é uma aplicação desenvolvida com Spring Boot para gerenciar e coordenar a geração de certificados digitais personalizados para eventos e usuários. O projeto utiliza uma arquitetura orientada a eventos com processamento assíncrono via Amazon SQS e funções AWS Lambda.

A API centraliza a criação de ordens de geração de certificados, delega o processamento a uma Lambda via fila SQS e acompanha o progresso por meio de eventos de retorno também enviados por outra fila SQS. Os certificados gerados são armazenados no Amazon S3, com URLs temporárias de acesso retornadas aos usuários.

## Tecnologias Utilizadas

- Java 24
- Spring Boot 3.4.4
- Spring Security (API Key Authentication)
- Spring Cloud AWS
- MongoDB
- Gradle
- Docker
- LocalStack (para ambiente de desenvolvimento)


## Visão Geral da Arquitetura

```text
[Client] --> [Certified Builder API]
                |
                v
        [SQS: build-order.fifo]
                |
                v
        [AWS Lambda: Cert Generator]
                |
                v
     Geração de certificados + Upload no S3
                |
                v
       [SQS: notification.fifo (callback)]
                |
                v
        [Certified Builder API] <-- Atualiza progresso e responde cliente
```

## Principais Funcionalidades

- **Criação de ordens de certificados** para produtos ou eventos
- **Processamento assíncrono** via Amazon SQS e AWS Lambda
- **Armazenamento de certificados** no Amazon S3
- **Monitoramento de progresso** da geração
- **Recuperação de certificados gerados**
- **Autenticação via API Key**

## Fluxo Detalhado

1. O cliente chama o endpoint `/certified/build-orders`, informando o `productId`.
2. A API envia uma mensagem para a **fila SQS de build orders**.
3. Uma **AWS Lambda** é acionada automaticamente, processa a ordem e gera os certificados.
4. Ao concluir a tarefa, a Lambda:
   - Armazena os certificados no S3.
   - Envia uma mensagem de retorno para a **fila SQS de notificações**, sinalizando o status (sucesso ou falha).
5. A API, que consome essa fila, atualiza o progresso e armazena os metadados no MongoDB.
6. O cliente pode consultar o progresso e os certificados gerados pelos endpoints `/statistics` e `/recover-certificates`.
 
## Pré-requisitos

- JDK 24
- Docker (opcional)
- Credenciais AWS configuradas
- MongoDB
- LocalStack (para ambiente de desenvolvimento)

## Configuração

### Variáveis de Ambiente

#### Ambiente de Desenvolvimento (LocalStack)
```bash
# AWS Credentials
AWS_ACCESS_KEY=your_access_key
AWS_SECRET_KEY=your_secret_key
AWS_REGION=your_region

# Queue Names
QUEUE_NAME_NOTIFICATION_GENERATION=your_notification_queue.fifo
QUEUE_NAME_BUILDER=your_builder_queue.fifo

# S3
S3_BUCKET_NAME=your_bucket_name

# Other Configurations
MONGODB_URI=your_mongodb_uri
URL_SERVICE_TECH=your_tech_service_url
API_KEY=your_api_key
```


### Configuração do LocalStack

O projeto utiliza o LocalStack para simular os serviços AWS em ambiente de desenvolvimento. Para configurar:

1. Instale o Docker e Docker Compose
2. Execute o comando para iniciar os serviços:
```bash
docker-compose up -d
```

O LocalStack irá:
- Criar as filas SQS necessárias
- Criar o bucket S3
- Configurar os endpoints locais para os serviços AWS

### AWS Services

A API utiliza os seguintes serviços AWS:
- SQS para processamento de mensagens
- S3 para armazenamento de arquivos

## Executando a Aplicação

### Localmente (com LocalStack)

```bash
# Iniciar LocalStack
docker-compose up -d

# Executar a aplicação
./gradlew bootRun -Dspring.profiles.active=dev
```

### Em Produção

```bash
# Executar a aplicação
./gradlew bootRun -Dspring.profiles.active=prod
```

### Com Docker

```bash
# Construir a imagem
docker build -t certified-builder-api .

# Executar o container
docker run -p 8081:8081 certified-builder-api
```

## Documentação

### Segurança
- Autenticação via API Key: [Documentação Spring Security API Key](https://www.baeldung.com/spring-boot-api-key-secret)

### AWS Services
- SQS: [Documentação Spring Cloud AWS SQS](https://www.baeldung.com/java-spring-cloud-aws-v3-intro)
- S3: [Documentação Spring Cloud AWS S3](https://docs.awspring.io/spring-cloud-aws/docs/3.3.0/reference/html/index.html#spring-cloud-aws-s3)

### LocalStack
- [Documentação Oficial do LocalStack](https://docs.localstack.cloud/user-guide/)
- [Guia de Serviços AWS Suportados](https://docs.localstack.cloud/user-guide/aws/feature-coverage/)
- [Configuração de Integração com Spring Boot](https://docs.localstack.cloud/user-guide/integrations/spring-boot/)

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
    "productId": 123
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
