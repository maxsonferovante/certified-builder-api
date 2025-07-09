# Solução para Problema de Timeout SQS em Produção

## Problema Identificado

A aplicação estava falhando na inicialização em produção com o seguinte erro:
```
QueueAttributesResolvingException: Error resolving attributes for queue notification_generation.fifo
SdkClientException: Unable to execute HTTP request: Acquire operation took longer than the configured maximum time
TimeoutException: Acquire operation took longer than 10000 milliseconds
```

## Causas Identificadas

1. **Cliente SQS sem configurações de timeout adequadas**
2. **Pool de conexões HTTP mal configurado**
3. **Estratégia CREATE tentando criar fila automaticamente**
4. **Falta de configurações de resiliência para conexões AWS**
5. **Listener SQS sem configurações de timeout específicas**

## Soluções Implementadas

### 1. Configuração Otimizada do Cliente SQS (`SQSConfig.java`)

**Alterações realizadas:**
- **Timeout de conexão**: 30 segundos (era 10s por padrão)
- **Timeout de aquisição de conexão**: 60 segundos (era 10s por padrão)
- **Pool de conexões concorrentes**: 100 conexões
- **Máximo de aquisições pendentes**: 10.000
- **Retry policy**: 3 tentativas com backoff exponencial
- **Timeout total da API**: 5 minutos
- **Timeout por tentativa**: 30 segundos

```java
// Cliente HTTP otimizado
NettyNioAsyncHttpClient.Builder httpClientBuilder = NettyNioAsyncHttpClient.builder()
    .connectionTimeout(Duration.ofSeconds(30))          // Timeout de conexão: 30s
    .connectionAcquisitionTimeout(Duration.ofSeconds(60)) // Timeout para adquirir conexão: 60s
    .maxConcurrency(100)                                 // Máximo de conexões concorrentes
    .maxPendingConnectionAcquires(10_000);               // Máximo de aquisições pendentes
```

### 2. Configurações de Produção (`application-prod.properties`)

**Novas configurações adicionadas:**
```properties
# Configurações SQS para resolver timeout e estratégias de acesso
spring.cloud.aws.sqs.listener.queue-not-found-strategy=fail
spring.cloud.aws.sqs.listener.poll-timeout=20
spring.cloud.aws.sqs.listener.max-messages-per-poll=10
spring.cloud.aws.sqs.listener.visibility-timeout=300
spring.cloud.aws.sqs.listener.wait-time-out=20

# Logs para debug
logging.level.io.awspring.cloud.sqs=INFO
logging.level.software.amazon.awssdk.services.sqs=INFO
```

### 3. Factory de Listener SQS Resiliente (`SqsConfig.java`)

**Configurações otimizadas:**
- **Concorrência reduzida**: 5 mensagens simultâneas (era 10)
- **Timeout de polling**: 20 segundos
- **Visibilidade de mensagens**: 5 minutos
- **Handler de erro customizado**: 3 tentativas com backoff
- **Timeout de shutdown**: 30 segundos

```java
.configure(options -> options
    .maxConcurrentMessages(5)              // Reduzido para evitar sobrecarga
    .pollTimeout(Duration.ofSeconds(20))   // Timeout para polling
    .messageVisibility(Duration.ofMinutes(5)) // 5 minutos para processar
    .errorHandler(createErrorHandler()))
```

### 4. Health Check AWS (`AwsHealthCheck.java`)

**Componente criado para:**
- Verificar conectividade na inicialização
- Detectar problemas de rede precocemente
- Logs detalhados para debug
- Verificações paralelas para SQS, DynamoDB e S3
- Timeout agressivo de 30 segundos para SQS

### 5. Listener Resiliente (`OrderEventListener.java`)

**Melhorias implementadas:**
- **Configurações específicas do listener**:
  - `deletionPolicy = "ON_SUCCESS"`: Remove apenas em sucesso
  - `maxConcurrentMessages = "3"`: Limite reduzido
  - `pollTimeoutSeconds = "20"`: Timeout de polling
  - `visibilityTimeoutSeconds = "300"`: 5 minutos para processar
  - `waitTimeoutSeconds = "20"`: Long polling

- **Retry automático**:
  ```java
  @Retryable(
      value = {Exception.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 2000, multiplier = 2) // 2s, 4s, 8s
  )
  ```

- **Processamento individual**: Falha em um evento não afeta o lote

## Configuração de Variáveis de Ambiente

Para produção, certifique-se de que as seguintes variáveis estejam configuradas:

```bash
# Credenciais AWS (obrigatórias)
AWS_ACCESS_KEY=sua_access_key
AWS_SECRET_KEY=sua_secret_key
AWS_REGION=us-east-1

# Filas SQS (obrigatórias) 
QUEUE_NAME_NOTIFICATION_GENERATION=notification_generation.fifo
QUEUE_NAME_BUILDER=builder.fifo

# Outros serviços
S3_BUCKET_NAME=seu_bucket
URL_SERVICE_TECH=sua_url_tech_service
API_KEY=sua_api_key
```

## Monitoramento e Logs

### Logs de Conectividade
```
INFO  - Iniciando verificações de conectividade AWS...
INFO  - Verificando conectividade SQS...
INFO  - ✓ Conectividade SQS verificada com sucesso
INFO  - ✓ Conectividade DynamoDB verificada com sucesso
INFO  - ✓ Conectividade S3 verificada com sucesso
INFO  - Verificações de conectividade AWS concluídas com sucesso
```

### Logs de Processamento SQS
```
INFO  - Recebida mensagem do SQS: [mensagem]
INFO  - Processando 3 eventos de ordem do SQS
INFO  - Processando evento de ordem: [evento]
INFO  - Evento de ordem processado com sucesso para orderId: 123
INFO  - Todos os eventos de ordem processados com sucesso
```

## Verificação da Solução

Para verificar se as configurações estão funcionando:

1. **Logs de inicialização**: Deve aparecer "Verificações de conectividade AWS concluídas com sucesso"
2. **Sem erros de timeout**: Não deve aparecer "TimeoutException" ou "Acquire operation took longer"
3. **Aplicação iniciada**: Status "Started CertifiedBuilderApiApplication"

## Troubleshooting

### Se ainda houver timeout:
1. Verificar conectividade de rede com AWS
2. Confirmar credenciais AWS válidas
3. Verificar se as filas SQS existem na região configurada
4. Aumentar timeouts se necessário
5. Verificar logs detalhados habilitados

### Configurações de emergência:
Se o problema persistir, é possível desabilitar temporariamente o SQS listener:
```properties
# Desabilitar listener SQS temporariamente
spring.cloud.aws.sqs.enabled=false
```

## Benefícios das Soluções

1. **Resiliência**: Retry automático e configurações de timeout adequadas
2. **Performance**: Pool de conexões otimizado para alta concorrência
3. **Monitoramento**: Health checks e logs detalhados
4. **Manutenibilidade**: Código bem estruturado com responsabilidades claras
5. **Produção-ready**: Configurações adequadas para ambiente de produção

## Próximos Passos

1. Monitorar logs de produção após o deploy
2. Ajustar timeouts conforme necessário baseado no comportamento real
3. Implementar métricas de monitoramento (CloudWatch)
4. Considerar implementar circuit breaker para ainda mais resiliência 