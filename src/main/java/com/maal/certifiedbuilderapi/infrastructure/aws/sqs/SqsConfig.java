package com.maal.certifiedbuilderapi.infrastructure.aws.sqs;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.listener.FifoBatchGroupingStrategy;
import io.awspring.cloud.sqs.listener.acknowledgement.AcknowledgementOrdering;
import io.awspring.cloud.sqs.listener.acknowledgement.handler.AcknowledgementMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class SqsConfig {

    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(
            SqsAsyncClient sqsAsyncClient) {
        return SqsMessageListenerContainerFactory
                .builder()
                .configure(options -> options
                        .maxConcurrentMessages(10)
                        /*
                        ​A estratégia FifoBatchGroupingStrategy.PROCESS_MESSAGE_GROUPS_IN_PARALLEL_BATCHES no Spring Cloud AWS configura o
                        processamento de mensagens em filas FIFO (First-In-First-Out) do Amazon SQS. Ao utilizar essa estratégia, as mensagens
                        são agrupadas em lotes com base no seu MessageGroupId, garantindo que cada lote contenha apenas mensagens de um único grupo.
                        Isso permite que diferentes grupos de mensagens sejam processados em paralelo, enquanto a ordem das mensagens dentro
                        de cada grupo é mantida. Essa abordagem é ideal para aplicações que precisam de alta taxa de transferência,
                        pois permite o processamento simultâneo de múltiplos grupos de mensagens, respeitando a ordem dentro de cada grupo.
                        * */
                        .fifoBatchGroupingStrategy(FifoBatchGroupingStrategy.PROCESS_MESSAGE_GROUPS_IN_PARALLEL_BATCHES)
                        .acknowledgementMode(AcknowledgementMode.ALWAYS)
                        .acknowledgementOrdering(AcknowledgementOrdering.ORDERED))
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }
} 