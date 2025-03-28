package com.maal.certifiedbuilderapi.infrastructure.aws.sqs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;



//POJO to represent these properties
@Getter

@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "events.queues")
public class EventQueuesProperties {
//    builder-queue
    @Value("${cloud.aws.sqs.queue-name}")
    private String builderQueueName;

}

