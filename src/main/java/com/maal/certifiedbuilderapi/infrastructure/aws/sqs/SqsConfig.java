package com.maal.certifiedbuilderapi.infrastructure.aws.sqs;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
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
                        .acknowledgementMode(AcknowledgementMode.ALWAYS)
                        .acknowledgementOrdering(AcknowledgementOrdering.ORDERED))
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }
} 