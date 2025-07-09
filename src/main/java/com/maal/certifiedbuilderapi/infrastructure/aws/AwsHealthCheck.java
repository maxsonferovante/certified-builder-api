package com.maal.certifiedbuilderapi.infrastructure.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.ListQueuesRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import lombok.RequiredArgsConstructor;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Componente responsável por verificar a conectividade com os serviços AWS
 * Executa verificações na inicialização da aplicação para detectar problemas de conectividade
 * @Profile("!test") garante que não seja carregado durante testes
 */
@Component
@RequiredArgsConstructor
@Profile("!test")
public class AwsHealthCheck {

    private static final Logger logger = LoggerFactory.getLogger(AwsHealthCheck.class);
    
    private final SqsAsyncClient sqsAsyncClient;
    private final DynamoDbClient dynamoDbClient;
    private final S3Client s3Client;

    /**
     * Executa verificações de conectividade após a inicialização do bean
     * Logs detalhados para facilitar debug em caso de problemas
     */
    @PostConstruct
    public void performHealthChecks() {
        logger.info("Iniciando verificações de conectividade AWS...");
        
        // Executa todas as verificações em paralelo para reduzir tempo de inicialização
        CompletableFuture.allOf(
            checkSqsConnectivity(),
            checkDynamoDbConnectivity(), 
            checkS3Connectivity()
        ).join();
        
        logger.info("Verificações de conectividade AWS concluídas com sucesso");
    }

    /**
     * Verifica conectividade com SQS
     * @return CompletableFuture<Void>
     */
    private CompletableFuture<Void> checkSqsConnectivity() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("Verificando conectividade SQS...");
                
                // Timeout mais agressivo para detectar problemas rapidamente
                sqsAsyncClient.listQueues(ListQueuesRequest.builder().build())
                    .orTimeout(30, TimeUnit.SECONDS)
                    .join();
                    
                logger.info("✓ Conectividade SQS verificada com sucesso");
            } catch (Exception e) {
                logger.error("✗ Falha na conectividade SQS: {}", e.getMessage());
                // Log adicional para debug
                if (e.getCause() != null) {
                    logger.error("Causa raiz: {}", e.getCause().getMessage());
                }
                throw new RuntimeException("Falha na conectividade SQS", e);
            }
        });
    }

    /**
     * Verifica conectividade com DynamoDB
     * @return CompletableFuture<Void>
     */
    private CompletableFuture<Void> checkDynamoDbConnectivity() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("Verificando conectividade DynamoDB...");
                
                dynamoDbClient.listTables(ListTablesRequest.builder().build());
                
                logger.info("✓ Conectividade DynamoDB verificada com sucesso");
            } catch (Exception e) {
                logger.error("✗ Falha na conectividade DynamoDB: {}", e.getMessage());
                // Em ambiente de produção, DynamoDB é crítico, mas não impede inicialização
                logger.warn("Continuando inicialização apesar do erro no DynamoDB");
            }
        });
    }

    /**
     * Verifica conectividade com S3
     * @return CompletableFuture<Void>
     */
    private CompletableFuture<Void> checkS3Connectivity() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("Verificando conectividade S3...");
                
                s3Client.listBuckets(ListBucketsRequest.builder().build());
                
                logger.info("✓ Conectividade S3 verificada com sucesso");
            } catch (Exception e) {
                logger.error("✗ Falha na conectividade S3: {}", e.getMessage());
                // S3 não é crítico para inicialização, apenas log do erro
                logger.warn("Continuando inicialização apesar do erro no S3");
            }
        });
    }
} 