package com.maal.certifiedbuilderapi.infrastructure.aws.sqs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

/**
 * Propriedades das filas SQS para ambientes de produção/desenvolvimento
 * @Profile("!test") garante que não seja carregada durante testes
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "events.queues")
@Profile("!test")
public class EventQueuesProperties {
//    builder-queue
    @Value("${spring.cloud.aws.sqs.queue-name}")
    private String builderQueueName;

}

