package com.maal.certifiedbuilderapi.infrastructure.aws.sqs;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.listener.acknowledgement.AcknowledgementOrdering;
import io.awspring.cloud.sqs.listener.acknowledgement.handler.AcknowledgementMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

/**
 * Configuração SQS para ambiente de produção/desenvolvimento
 * @Profile("!test") garante que não seja carregada durante testes
 * Responsabilidade única: configurar factory de listener SQS
 */
@Configuration
@Profile("!test")
public class SqsConfig {

    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(
            SqsAsyncClient sqsAsyncClient) {
        return SqsMessageListenerContainerFactory
                .builder()
                .configure(options -> options
                        .maxConcurrentMessages(10)
                        .acknowledgementMode(AcknowledgementMode.ALWAYS)
                        .acknowledgementOrdering(AcknowledgementOrdering.ORDERED))
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }
} 