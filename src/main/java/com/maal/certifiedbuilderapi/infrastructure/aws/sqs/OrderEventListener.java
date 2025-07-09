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
import org.springframework.scheduling.annotation.Async;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Listener for order events from SQS queue.
 * Processes order events and manages certificate generation.
 * @Profile("!test") garante que o listener não seja carregado em testes
 * Configurado com resiliência e timeouts otimizados através do SqsConfig.java
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
     * Configurações de resiliência são definidas centralmente no SqsConfig.java
     *
     * @param messageBody The message body containing the array of order events
     */
    @SqsListener(value = "${spring.cloud.aws.queue.name.notification.generation}")
    @Async
    @Retryable(
            value = {Exception.class},      // Retry para qualquer exceção
            maxAttempts = 3,                // Máximo 3 tentativas
            backoff = @Backoff(delay = 2000, multiplier = 2) // Backoff exponencial: 2s, 4s, 8s
    )
    public void receiveOrderEvent(String messageBody) {
        try {
            logger.info("Recebida mensagem do SQS: {}", messageBody);
            
            // Convert the message body to a list of OrderEvent objects
            List<OrderEvent> ordersEvent = objectMapper.readValue(
                messageBody,
                new TypeReference<List<OrderEvent>>() {}
            );
            
            logger.info("Processando {} eventos de ordem do SQS", ordersEvent.size());
            
            // Processa cada evento com tratamento individual de erros
            for (OrderEvent orderEvent : ordersEvent) {
                processIndividualOrderEvent(orderEvent);
            }
            
            logger.info("Todos os eventos de ordem processados com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao processar lote de ordens: {}", e.getMessage(), e);
            // Re-throw para permitir retry automático
            throw new RuntimeException("Falha no processamento do lote de ordens", e);
        }
    }

    /**
     * Processa um evento individual com tratamento de erro isolado
     * @param orderEvent Evento a ser processado
     */
    private void processIndividualOrderEvent(OrderEvent orderEvent) {
        try {
            logger.info("Processando evento de ordem: {}", orderEvent);
            processOrderEvent.execute(orderEvent);
            logger.info("Evento de ordem processado com sucesso para orderId: {}", orderEvent.getOrderId());
        } catch (Exception e) {
            logger.error("Erro ao processar evento individual para orderId {}: {}", 
                orderEvent.getOrderId(), e.getMessage(), e);
            // Não re-throw aqui para continuar processando outros eventos do lote
        }
    }
}
