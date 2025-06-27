package com.maal.certifiedbuilderapi.infrastructure.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

/**
 * Configuração centralizada para serviços AWS usando SDK v2.x
 * Responsabilidade única: gerenciar credenciais, região e configurações comuns
 * Todos os beans são compatíveis com AWS SDK v2.x
 */
@Configuration
@Profile("!test")
public class AwsConfig {

    // === CREDENCIAIS AWS CENTRALIZADAS ===
    @Value("${spring.cloud.aws.credentials.accessKey}")
    private String accessKeyId;

    @Value("${spring.cloud.aws.credentials.secretKey}")
    private String secretAccessKey;

    // === REGIÃO AWS CENTRALIZADA ===
    @Value("${spring.cloud.aws.region.static}")
    private String region;

    // === ENDPOINT GERAL (LocalStack) ===
    @Value("${spring.cloud.aws.endpoint:}")
    private String endpoint;

    /**
     * Bean de credenciais AWS (SDK v2.x) - compartilhado entre serviços
     * Usado por DynamoDB, SQS, S3 e outros serviços AWS
     * @return AwsCredentials configuradas com access key e secret key
     */
    @Bean
    public AwsCredentials awsCredentials() {
        return AwsBasicCredentials.create(accessKeyId, secretAccessKey);
    }

    /**
     * Provedor de credenciais estático (SDK v2.x)
     * @param awsCredentials Bean de credenciais injetado
     * @return StaticCredentialsProvider configurado
     */
    @Bean
    public StaticCredentialsProvider awsCredentialsProvider(AwsCredentials awsCredentials) {
        return StaticCredentialsProvider.create(awsCredentials);
    }

    /**
     * Bean para região AWS (SDK v2.x)
     * @return Region configurada
     */
    @Bean
    public Region awsRegion() {
        return Region.of(region);
    }

    /**
     * Bean para obter a região como String (compatibilidade)
     * @return Região AWS como string
     */
    @Bean
    public String awsRegionString() {
        return region;
    }

    /**
     * Bean para obter o endpoint geral (LocalStack)
     * @return Endpoint configurado ou string vazia
     */
    @Bean
    public String awsEndpoint() {
        return endpoint;
    }
}
