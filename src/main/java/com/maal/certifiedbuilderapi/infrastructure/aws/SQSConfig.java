package com.maal.certifiedbuilderapi.infrastructure.aws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import java.net.URI;

/**
 * Configuração específica do Amazon SQS
 * Utiliza beans injetados da AwsConfig para credenciais e região
 * Responsabilidade única: configurar cliente SQS
 */
@Profile("!test")
@Configuration
public class SQSConfig {

    /**
     * Cliente SQS Async configurado
     * Injeta credenciais, região e endpoint dos beans centralizados
     * 
     * @param awsRegion Região AWS injetada da AwsConfig
     * @param awsEndpoint Endpoint geral injetado da AwsConfig (para LocalStack)
     * @param awsCredentials Credenciais AWS injetadas da AwsConfig
     * @return SqsAsyncClient configurado
     */
    @Bean
    public SqsAsyncClient sqsAsyncClient(
            Region awsRegion,
            String awsEndpoint,
            StaticCredentialsProvider awsCredentialsProvider) {
        
        var builder = SqsAsyncClient.builder()
                .region(awsRegion)
                .credentialsProvider(awsCredentialsProvider);

        // Configurar endpoint apenas se estiver definido (LocalStack)
        if (!awsEndpoint.isEmpty()) {
            builder.endpointOverride(URI.create(awsEndpoint));
        }

        return builder.build();
    }
} 