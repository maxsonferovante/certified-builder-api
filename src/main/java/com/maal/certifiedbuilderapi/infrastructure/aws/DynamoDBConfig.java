package com.maal.certifiedbuilderapi.infrastructure.aws;

import com.maal.certifiedbuilderapi.domain.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

/**
 * Configuração DynamoDB para AWS SDK v2.x
 * Usa Enhanced DynamoDB Client para melhor experiência de desenvolvimento
 * Reutiliza beans centralizados do AwsConfig (credenciais, região)
 */
@Profile("!test")
@Configuration
public class DynamoDBConfig {

    @Value("${amazon.dynamodb.endpoint:}")
    private String dynamodbEndpoint;

    @Autowired
    private StaticCredentialsProvider awsCredentialsProvider;
    
    @Autowired
    private Region awsRegion;

    /**
     * Cliente DynamoDB usando AWS SDK v2.x
     * Usa beans centralizados do AwsConfig para credenciais e região
     */
    @Bean
    public DynamoDbClient dynamoDbClient() {
        var builder = DynamoDbClient.builder()
            .credentialsProvider(awsCredentialsProvider)
            .region(awsRegion);

        // Configurar endpoint local se estiver definido (desenvolvimento)
        if (!dynamodbEndpoint.isEmpty()) {
            builder.endpointOverride(URI.create(dynamodbEndpoint));
        }

        return builder.build();
    }

    /**
     * Enhanced DynamoDB Client para operações de alto nível
     */
    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
            .dynamoDbClient(dynamoDbClient)
            .build();
    }

    /**
     * Tabela Certificate com schema mapeado
     */
    @Bean
    public DynamoDbTable<CertificateEntity> certificateTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table("certificates", TableSchema.fromBean(CertificateEntity.class));
    }

    /**
     * Tabela Order com schema mapeado
     */
    @Bean
    public DynamoDbTable<OrderEntity> orderTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table("orders", TableSchema.fromBean(OrderEntity.class));
    }

    /**
     * Tabela Participant com schema mapeado
     */
    @Bean
    public DynamoDbTable<ParticipantEntity> participantTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table("participants", TableSchema.fromBean(ParticipantEntity.class));
    }

    /**
     * Tabela Product com schema mapeado
     */
    @Bean
    public DynamoDbTable<ProductEntity> productTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table("products", TableSchema.fromBean(ProductEntity.class));
    }
} 