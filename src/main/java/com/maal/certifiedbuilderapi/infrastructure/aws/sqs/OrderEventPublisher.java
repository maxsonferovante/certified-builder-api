package com.maal.certifiedbuilderapi.infrastructure.aws.sqs;

import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class OrderEventPublisher {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(OrderEventPublisher.class);

    private final SqsTemplate sqsTemplate;
    private final EventQueuesProperties eventQueuesProperties;

    public void publishOrderCreatedEvent(List<TechOrdersResponse>  techOrdersResponses) {
        sqsTemplate.send(
                to -> to.queue(eventQueuesProperties.getBuilderQueueName())
                        .payload(techOrdersResponses)
        );
        logger.debug("Order Created Event Published");
    }
}
