package com.maal.certifiedbuilderapi.infrastructure.aws.sqs;

import com.maal.certifiedbuilderapi.domain.event.OrderEvent;
import com.maal.certifiedbuilderapi.business.usecase.certificate.ProcessOrderEvent;
import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.context.annotation.Profile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Listener for order events from SQS queue.
 * Processes order events and manages certificate generation.
 * @Profile("!test") garante que o listener não seja carregado em testes
 */
@Component
@RequiredArgsConstructor
@Profile("!test")
public class OrderEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);
    
    private final ProcessOrderEvent processOrderEvent;
    private final ObjectMapper objectMapper;

    /**
     * Listens for order events from the SQS queue and processes them.
     *
     * @param messageBody The message body containing the array of order events
     */
    @SqsListener(
            value = "${spring.cloud.aws.queue.name.notification.generation}"
    )
    public void receiveOrderEvent(String messageBody) {
        try {
            logger.info("Received message body from SQS: {}", messageBody);
            
            // Convert the message body to a list of OrderEvent objects
            List<OrderEvent> ordersEvent = objectMapper.readValue(
                messageBody,
                new TypeReference<List<OrderEvent>>() {}
            );
            
            logger.info("Processed {} order events from SQS", ordersEvent.size());
            
            for (OrderEvent orderEvent : ordersEvent) {
                logger.info("Processing order event: {}", orderEvent);
                processOrderEvent.execute(orderEvent);
                logger.info("Successfully processed order event for orderId: {}", orderEvent.getOrderId());
            }
        } catch (Exception e) {
            logger.error("Error processing batch of orders", e);
        }
    }
}
