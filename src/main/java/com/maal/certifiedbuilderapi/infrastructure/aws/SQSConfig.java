package com.maal.certifiedbuilderapi.infrastructure.aws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;
import software.amazon.awssdk.core.retry.backoff.BackoffStrategy;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import java.net.URI;
import java.time.Duration;

/**
 * Configuração específica do Amazon SQS
 * Utiliza beans injetados da AwsConfig para credenciais e região
 * Responsabilidade única: configurar cliente SQS com configurações de timeout otimizadas
 */
@Profile("!test")
@Configuration
public class SQSConfig {

    /**
     * Cliente SQS Async configurado com timeouts e pool de conexões otimizados
     * Injeta credenciais, região e endpoint dos beans centralizados
     * 
     * @param awsRegion Região AWS injetada da AwsConfig
     * @param awsEndpoint Endpoint geral injetado da AwsConfig (para LocalStack)
     * @return SqsAsyncClient configurado com timeouts adequados
     */
    @Bean
    public SqsAsyncClient sqsAsyncClient(
            Region awsRegion,
            String awsEndpoint,
            StaticCredentialsProvider awsCredentialsProvider) {
        
        // Configuração do cliente HTTP com timeouts e pool de conexões otimizados
        NettyNioAsyncHttpClient.Builder httpClientBuilder = NettyNioAsyncHttpClient.builder()
                .connectionTimeout(Duration.ofSeconds(30))          // Timeout de conexão: 30s
                .connectionAcquisitionTimeout(Duration.ofSeconds(60)) // Timeout para adquirir conexão do pool: 60s
                .maxConcurrency(100)                                 // Máximo de conexões concorrentes
                .maxPendingConnectionAcquires(10_000);               // Máximo de aquisições pendentes

        // Configuração de retry policy para melhor resiliência
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .numRetries(3)                                       // Máximo 3 tentativas
                .retryCondition(RetryCondition.defaultRetryCondition())
                .backoffStrategy(BackoffStrategy.defaultStrategy())  // Backoff exponencial
                .build();

        // Configuração de override do cliente para timeouts de API
        ClientOverrideConfiguration.Builder overrideConfig = ClientOverrideConfiguration.builder()
                .apiCallTimeout(Duration.ofMinutes(5))               // Timeout total da chamada: 5 minutos
                .apiCallAttemptTimeout(Duration.ofSeconds(30))       // Timeout por tentativa: 30s
                .retryPolicy(retryPolicy);

        var builder = SqsAsyncClient.builder()
                .region(awsRegion)
                .credentialsProvider(awsCredentialsProvider)
                .httpClient(httpClientBuilder.build())               // Cliente HTTP otimizado
                .overrideConfiguration(overrideConfig.build());     // Configurações de timeout

        // Configurar endpoint apenas se estiver definido (LocalStack)
        if (!awsEndpoint.isEmpty()) {
            builder.endpointOverride(URI.create(awsEndpoint));
        }

        return builder.build();
    }
} 