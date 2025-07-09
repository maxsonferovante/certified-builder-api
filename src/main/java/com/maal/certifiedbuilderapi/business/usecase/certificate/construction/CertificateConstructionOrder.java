package com.maal.certifiedbuilderapi.business.usecase.certificate.construction;

import com.maal.certifiedbuilderapi.business.dto.BuildOrdersRequest;
import com.maal.certifiedbuilderapi.business.dto.BuildOrdersResponse;
import com.maal.certifiedbuilderapi.infrastructure.aws.sqs.OrderEventPublisher;
import com.maal.certifiedbuilderapi.infrastructure.client.TechFloripa;
import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import org.slf4j.Logger;


/**
 * Use case para construção de ordens de certificados
 * Refatorado para usar separação de responsabilidades e melhor organização
 * 
 * Responsabilidades:
 * - Coordenar o fluxo principal de processamento
 * - Integrar com TechFloripa para buscar ordens
 * - Publicar eventos para novas ordens
 * - Construir resposta final
 */
@Service
@RequiredArgsConstructor
public class CertificateConstructionOrder {

    private static final Logger logger = LoggerFactory.getLogger(CertificateConstructionOrder.class);

    private final TechFloripa techFloripa;
    private final OrderEventPublisher orderEventPublisher;
    private final OrderProcessingService orderProcessingService;

    /**
     * Processes orders from TechFloripa and creates certificates for new orders.
     *
     * @param orders The list of TechOrdersResponse containing the product ID
     * @return BuildOrdersResponse with processing results
     */
    public BuildOrdersResponse execute(List<TechOrdersResponse> orders) {
        logger.info("Executing construction order for {} orders", orders.size());

        if (CollectionUtils.isEmpty(orders)) {
            logger.warn("No orders found");
            return BuildOrdersResponse.builder()
                    .certificateQuantity(0)
                    .existingOrders(List.of())
                    .newOrders(List.of())
                    .build();
        }
        
        // Processa ordens usando o serviço especializado
        ProcessingResult result = orderProcessingService.processOrdersParallel(orders);
        
        // Publica eventos para novas ordens
        publishNewOrdersIfNeeded(result.getNewOrders());

        return buildResponse(result.getExistingOrders(), result.getNewOrders());
    }
    
    /**
     * Processes orders from TechFloripa and creates certificates for new orders.
     *
     * @param request The build orders request containing the product ID
     * @return BuildOrdersResponse with processing results
     */
    public BuildOrdersResponse execute(BuildOrdersRequest request) {
        logger.info("Executing construction order for productId: {}", request.getProductId());
        
        // Busca ordens da TechFloripa
        List<TechOrdersResponse> orders = techFloripa.getOrders(request.getProductId());
        
        if (CollectionUtils.isEmpty(orders)) {
            logger.info("No orders found for productId: {}", request.getProductId());
            return BuildOrdersResponse.builder()
                    .certificateQuantity(0)
                    .existingOrders(List.of())
                    .newOrders(List.of())
                    .build();
        }
        
        // Delega para o método que processa lista de ordens
        return execute(orders);
    }

    /**
     * Publica eventos para novas ordens se houver alguma
     * 
     * @param newOrders Lista de novas ordens processadas
     */
    private void publishNewOrdersIfNeeded(List<TechOrdersResponse> newOrders) {
        if (CollectionUtils.isEmpty(newOrders)) {
            logger.debug("No new orders to publish events for");
            return;
        }
        
        logger.info("Publishing events for {} new orders", newOrders.size());
        orderEventPublisher.publishOrderCreatedEvent(newOrders);
    }

    /**
     * Constrói resposta final com estatísticas do processamento
     * 
     * @param existingOrders Lista de IDs de ordens que já existiam
     * @param newOrders Lista de novas ordens processadas
     * @return BuildOrdersResponse com resultados
     */
    private BuildOrdersResponse buildResponse(List<Integer> existingOrders,
                                            List<TechOrdersResponse> newOrders) {
        return BuildOrdersResponse.builder()
                .certificateQuantity(newOrders.size())
                .existingOrders(existingOrders)
                .newOrders(newOrders.stream().map(TechOrdersResponse::getOrderId).toList())
                .build();
    }
} 