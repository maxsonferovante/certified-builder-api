package com.maal.certifiedbuilderapi.config;

import com.maal.certifiedbuilderapi.domain.entity.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import com.maal.certifiedbuilderapi.infrastructure.aws.sqs.OrderEventPublisher;
import io.awspring.cloud.s3.S3Template;
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Configuração específica para testes
 * Fornece beans mock para dependências AWS, evitando conexões reais durante testes
 * Responsabilidade: isolar testes de dependências externas (AWS, SQS, S3)
 */
@TestConfiguration
@Profile("test")
public class TestConfig {

    /**
     * Mock das credenciais AWS para testes
     * Evita tentativas de conexão real com AWS durante os testes
     */
    @Bean
    @Primary
    public AwsCredentials awsCredentials() {
        return AwsBasicCredentials.create("test-access-key", "test-secret-key");
    }

    /**
     * Mock do provedor de credenciais para testes
     */
    @Bean
    @Primary 
    public StaticCredentialsProvider awsCredentialsProvider() {
        return StaticCredentialsProvider.create(awsCredentials());
    }

    /**
     * Mock da região AWS para testes
     */
    @Bean
    @Primary
    public Region awsRegion() {
        return Region.US_EAST_1;
    }

    /**
     * Mock do cliente DynamoDB para testes
     * Evita tentativas de conexão real com DynamoDB
     */
    @Bean
    @Primary
    public DynamoDbClient dynamoDbClient() {
        return mock(DynamoDbClient.class);
    }

    /**
     * Mock do cliente Enhanced DynamoDB para testes
     */
    @Bean
    @Primary
    public DynamoDbEnhancedClient dynamoDbEnhancedClient() {
        return mock(DynamoDbEnhancedClient.class);
    }

    /**
     * Mock da tabela Certificate para testes
     */
    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public DynamoDbTable<CertificateEntity> certificateTable() {
        return mock(DynamoDbTable.class);
    }

    /**
     * Mock da tabela Order para testes
     */
    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public DynamoDbTable<OrderEntity> orderTable() {
        return mock(DynamoDbTable.class);
    }

    /**
     * Mock da tabela Participant para testes
     */
    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public DynamoDbTable<ParticipantEntity> participantTable() {
        return mock(DynamoDbTable.class);
    }

    /**
     * Mock da tabela Product para testes
     */
    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public DynamoDbTable<ProductEntity> productTable() {
        return mock(DynamoDbTable.class);
    }

    /**
     * Bean para string da região AWS (compatibilidade)
     */
    @Bean
    @Primary
    public String awsRegionString() {
        return "us-east-1";
    }

    /**
     * Bean para endpoint AWS (testes locais)
     */
    @Bean
    @Primary
    public String awsEndpoint() {
        return "";
    }

    /**
     * Mock do cliente SQS assíncrono para testes
     * Evita tentativas de conexão real com SQS
     */
    @Bean
    @Primary
    public SqsAsyncClient sqsAsyncClient() {
        return mock(SqsAsyncClient.class);
    }

    /**
     * Mock do factory de container de listener SQS para testes
     */
    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory() {
        return mock(SqsMessageListenerContainerFactory.class);
    }

    /**
     * Mock do cliente S3 para testes
     * Evita tentativas de conexão real com S3
     */
    @Bean
    @Primary
    public S3Client s3Client() {
        return mock(S3Client.class);
    }

    /**
     * Mock do template S3 para testes
     */
    @Bean
    @Primary
    public S3Template s3Template() {
        return mock(S3Template.class);
    }

    /**
     * Mock do SqsTemplate para testes
     * Evita tentativas de conexão real com SQS para envio de mensagens
     */
    @Bean
    @Primary
    public SqsTemplate sqsTemplate() {
        return mock(SqsTemplate.class);
    }

    /**
     * Mock do OrderEventPublisher para testes
     * Como o publisher real tem @Profile("!test"), este mock será usado nos testes
     */
    @Bean
    @Primary
    public OrderEventPublisher orderEventPublisher() {
        return mock(OrderEventPublisher.class);
    }

    /**
     * Mock do S3Properties para testes
     * Como o real tem @Profile("!test"), este mock será usado nos testes
     */
    @Bean
    @Primary
    public com.maal.certifiedbuilderapi.infrastructure.aws.s3.S3Properties s3Properties() {
        var mock = mock(com.maal.certifiedbuilderapi.infrastructure.aws.s3.S3Properties.class);
        when(mock.getBucketName()).thenReturn("test-bucket-mock");
        return mock;
    }

    /**
     * Mock do S3ClientCustomer para testes
     * Como o real tem @Profile("!test"), este mock será usado nos testes
     */
    @Bean
    @Primary
    public com.maal.certifiedbuilderapi.infrastructure.aws.s3.S3ClientCustomer s3ClientCustomer() {
        return mock(com.maal.certifiedbuilderapi.infrastructure.aws.s3.S3ClientCustomer.class);
    }

    /**
     * Mock do EventQueuesProperties para testes
     * Como o real tem @Profile("!test"), este mock será usado nos testes
     */
    @Bean
    @Primary
    public com.maal.certifiedbuilderapi.infrastructure.aws.sqs.EventQueuesProperties eventQueuesProperties() {
        var mock = mock(com.maal.certifiedbuilderapi.infrastructure.aws.sqs.EventQueuesProperties.class);
        when(mock.getBuilderQueueName()).thenReturn("test-queue-mock.fifo");
        return mock;
    }
} 