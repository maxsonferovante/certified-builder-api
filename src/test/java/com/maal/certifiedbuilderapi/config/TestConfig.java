package com.maal.certifiedbuilderapi.config;

import com.maal.certifiedbuilderapi.domain.entity.CertificateEntity;
import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import com.maal.certifiedbuilderapi.domain.entity.ParticipantEntity;
import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import static org.mockito.Mockito.mock;

/**
 * Configuração específica para testes
 * Fornece mocks dos clientes AWS e tabelas DynamoDB para evitar conexões reais durante os testes
 * 
 * @Profile("test") garante que seja carregada apenas em testes
 */
@TestConfiguration
@Profile("test")
public class TestConfig {

    /**
     * Mock do cliente DynamoDB para testes
     * @Primary garante prioridade sobre outras configurações
     */
    @Bean
    @Primary
    public DynamoDbClient mockDynamoDbClient() {
        return mock(DynamoDbClient.class);
    }

    /**
     * Mock do Enhanced DynamoDB Client para testes
     * @Primary garante prioridade sobre outras configurações
     */
    @Bean
    @Primary
    public DynamoDbEnhancedClient mockDynamoDbEnhancedClient() {
        return mock(DynamoDbEnhancedClient.class);
    }

    /**
     * Mock da tabela Certificate para testes
     * @Primary garante prioridade sobre outras configurações
     */
    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public DynamoDbTable<CertificateEntity> mockCertificateTable() {
        return mock(DynamoDbTable.class);
    }

    /**
     * Mock da tabela Order para testes
     * @Primary garante prioridade sobre outras configurações
     */
    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public DynamoDbTable<OrderEntity> mockOrderTable() {
        return mock(DynamoDbTable.class);
    }

    /**
     * Mock da tabela Participant para testes
     * @Primary garante prioridade sobre outras configurações
     */
    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public DynamoDbTable<ParticipantEntity> mockParticipantTable() {
        return mock(DynamoDbTable.class);
    }

    /**
     * Mock da tabela Product para testes
     * @Primary garante prioridade sobre outras configurações
     */
    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public DynamoDbTable<ProductEntity> mockProductTable() {
        return mock(DynamoDbTable.class);
    }

    /**
     * Mock do cliente S3 para testes
     * @Primary garante prioridade sobre outras configurações
     */
    @Bean
    @Primary
    public S3Client mockS3Client() {
        return mock(S3Client.class);
    }

    /**
     * Mock do cliente SQS Async para testes
     * @Primary garante prioridade sobre outras configurações
     */
    @Bean
    @Primary
    public SqsAsyncClient mockSqsAsyncClient() {
        return mock(SqsAsyncClient.class);
    }
} 