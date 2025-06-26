package com.maal.certifiedbuilderapi.config;

import com.maal.certifiedbuilderapi.domain.entity.CertificateEntity;
import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import com.maal.certifiedbuilderapi.domain.entity.ParticipantEntity;
import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import com.maal.certifiedbuilderapi.infrastructure.aws.s3.S3Properties;
import com.maal.certifiedbuilderapi.infrastructure.aws.sqs.EventQueuesProperties;
import io.awspring.cloud.s3.S3Template;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

@TestConfiguration
@Profile("test")
public class AwsTestConfig {

    @Bean
    @Primary
    public AwsCredentials awsCredentials() {
        return AwsBasicCredentials.create("test", "test");
    }

    @Bean
    @Primary
    public StaticCredentialsProvider awsCredentialsProvider(AwsCredentials awsCredentials) {
        return StaticCredentialsProvider.create(awsCredentials);
    }

    @Bean
    @Primary
    public Region awsRegion() {
        return Region.US_EAST_1;
    }

    @Bean
    @Primary
    public String awsRegionString() {
        return "us-east-1";
    }

    @Bean
    @Primary
    public String awsEndpoint() {
        return "http://localhost:4566";
    }

    @Bean
    @Primary
    public DynamoDbClient dynamoDbClient(StaticCredentialsProvider credentialsProvider, Region region) {
        return DynamoDbClient.builder()
            .credentialsProvider(credentialsProvider)
            .region(region)
            .endpointOverride(URI.create("http://localhost:4566"))
            .build();
    }

    @Bean
    @Primary
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
            .dynamoDbClient(dynamoDbClient)
            .build();
    }

    @Bean
    @Primary
    public DynamoDbTable<CertificateEntity> certificateTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table("certificates", TableSchema.fromBean(CertificateEntity.class));
    }

    @Bean
    @Primary
    public DynamoDbTable<OrderEntity> orderTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table("orders", TableSchema.fromBean(OrderEntity.class));
    }

    @Bean
    @Primary
    public DynamoDbTable<ParticipantEntity> participantTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table("participants", TableSchema.fromBean(ParticipantEntity.class));
    }

    @Bean
    @Primary
    public DynamoDbTable<ProductEntity> productTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table("products", TableSchema.fromBean(ProductEntity.class));
    }

    @Bean
    @Primary
    public SqsAsyncClient sqsAsyncClient(StaticCredentialsProvider credentialsProvider, Region region) {
        return SqsAsyncClient.builder()
            .credentialsProvider(credentialsProvider)
            .region(region)
            .endpointOverride(URI.create("http://localhost:4566"))
            .build();
    }

    @Bean
    @Primary
    public S3Client s3Client(StaticCredentialsProvider credentialsProvider, Region region) {
        return S3Client.builder()
            .credentialsProvider(credentialsProvider)
            .region(region)
            .endpointOverride(URI.create("http://localhost:4566"))
            .build();
    }

    @Bean
    @Primary
    public S3Presigner s3Presigner(StaticCredentialsProvider credentialsProvider, Region region) {
        return S3Presigner.builder()
            .credentialsProvider(credentialsProvider)
            .region(region)
            .endpointOverride(URI.create("http://localhost:4566"))
            .build();
    }

    @Bean
    @Primary
    public S3Template s3Template() {
        // Usar Mockito para criar um mock do S3Template para testes
        return org.mockito.Mockito.mock(S3Template.class);
    }

    @Bean
    @Primary
    public S3Properties s3Properties() {
        S3Properties properties = new S3Properties();
        properties.setBucketName("test-bucket");
        return properties;
    }

    @Bean
    @Primary
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
        return SqsTemplate.builder()
            .sqsAsyncClient(sqsAsyncClient)
            .build();
    }

    @Bean
    @Primary
    public EventQueuesProperties eventQueuesProperties() {
        EventQueuesProperties properties = new EventQueuesProperties();
        properties.setBuilderQueueName("test-queue.fifo");
        return properties;
    }
} 