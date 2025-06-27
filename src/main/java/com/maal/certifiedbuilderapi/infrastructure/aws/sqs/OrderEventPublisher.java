package com.maal.certifiedbuilderapi.infrastructure.aws.sqs;

import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service responsible for publishing order events to SQS queue.
 * @Profile("!test") garante que o publisher não seja carregado em testes
 */
@Service
@RequiredArgsConstructor
@Profile("!test")
public class OrderEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final SqsTemplate sqsTemplate;
    private final EventQueuesProperties eventQueuesProperties;

    /**
     * Publishes a list of order events to the SQS queue.
     *
     * @param orders The list of orders to publish
     * @throws RuntimeException if there's an error publishing the events
     */
    public void publishOrderCreatedEvent(List<TechOrdersResponse> orders) {
        try {
            logger.info("Publishing {} orders to queue: {} ", orders.size(), eventQueuesProperties.getBuilderQueueName());
            
            String messageGroupId = UUID.randomUUID().toString();
            String deduplicationId = UUID.randomUUID().toString();

            UUID messageId = sqsTemplate.send(
                to -> to.queue(eventQueuesProperties.getBuilderQueueName())
                        .payload(orders)
                        .messageGroupId(messageGroupId)
                        .messageDeduplicationId(deduplicationId)

            ).messageId();
            
            logger.info("Successfully published batch of {} orders to queue with message id {} and group id {}", 
                orders.size(), messageId, messageGroupId);
        } catch (Exception e) {
            logger.error("Error publishing orders to queue: {}", eventQueuesProperties.getBuilderQueueName(), e);
            throw new RuntimeException("Failed to publish orders to queue", e);
        }
    }
}
