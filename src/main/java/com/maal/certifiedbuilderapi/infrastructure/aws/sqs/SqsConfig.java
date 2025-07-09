package com.maal.certifiedbuilderapi.infrastructure.aws.sqs;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.listener.acknowledgement.AcknowledgementOrdering;
import io.awspring.cloud.sqs.listener.acknowledgement.handler.AcknowledgementMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;

/**
 * Configuração SQS para ambiente de produção/desenvolvimento
 * @Profile("!test") garante que não seja carregada durante testes
 * Responsabilidade única: configurar factory de listener SQS com resiliência otimizada
 * IMPORTANTE: messagesPerPoll deve ser <= maxConcurrentMessages
 */
@Configuration
@Profile("!test")
public class SqsConfig {

    private static final Logger logger = LoggerFactory.getLogger(SqsConfig.class);

    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(
            SqsAsyncClient sqsAsyncClient) {
        
        logger.info("Configurando SqsMessageListenerContainerFactory com timeouts otimizados");
        
        return SqsMessageListenerContainerFactory
                .builder()
                .configure(options -> options
                        // Configurações de concorrência otimizadas
                        .maxConcurrentMessages(10)             // Aumentado para acomodar messagesPerPoll
                        .maxMessagesPerPoll(5)                 // Menor que maxConcurrentMessages
                        
                        // Configurações de acknowledgement
                        .acknowledgementMode(AcknowledgementMode.ALWAYS)
                        .acknowledgementOrdering(AcknowledgementOrdering.ORDERED)
                        
                        // Configurações de timeout para polling
                        .pollTimeout(Duration.ofSeconds(10))   // Timeout para polling de mensagens
                        
                        // Configurações de visibilidade de mensagens
                        .messageVisibility(Duration.ofMinutes(5)) // 5 minutos para processar uma mensagem
                        
                        // Configurações de backoff para retry
                        .listenerShutdownTimeout(Duration.ofSeconds(30)))
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }
} 