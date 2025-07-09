package com.maal.certifiedbuilderapi.business.usecase.certificate.construction;

import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import com.maal.certifiedbuilderapi.domain.entity.ParticipantEntity;
import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço responsável pelo processamento paralelo de ordens
 * Coordena o processamento usando cache e criação de entidades
 */
@Service
@RequiredArgsConstructor
public class OrderProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderProcessingService.class);
    
    private final OrderRepository orderRepository;
    private final ProductParticipantCacheService cacheService;
    private final EntityCreationService entityCreationService;
    
    /**
     * Processa lista de ordens de forma paralela usando streams
     * Utiliza cache local para otimizar performance
     * 
     * @param orders Lista de ordens a serem processadas
     * @return ProcessingResult com ordens existentes e novas
     */
    public ProcessingResult processOrdersParallel(List<TechOrdersResponse> orders) {
        logger.info("Processing {} orders using parallel streams", orders.size());
        
        // Cria caches thread-safe para esta sessão de processamento
        ConcurrentHashMap<Integer, ProductEntity> productCache = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, ParticipantEntity> participantCache = new ConcurrentHashMap<>();
        
        ProcessingResult result = new ProcessingResult();
        
        // Filtra ordens válidas (com timeCheckin não vazio) e processa em paralelo
        orders.parallelStream()
            .filter(order -> !order.getTimeCheckin().isEmpty())
            .forEach(order -> {
                try {
                    // Busca ou cria produto usando cache local
                    ProductEntity product = cacheService.getOrCreateProductWithCache(order, productCache);
                    
                    // Busca ou cria participante usando cache local
                    ParticipantEntity participant = cacheService.getOrCreateParticipantWithCache(order, participantCache);
                    
                    // Processa a ordem individual
                    processIndividualOrder(order, product, participant, result);
                    
                } catch (Exception e) {
                    logger.error("Erro ao processar ordem {}: {}", order.getOrderId(), e.getMessage(), e);
                }
            });
        
        logger.info("Finished processing orders - New: {}, Existing: {}", 
                   result.getNewOrders().size(), result.getExistingOrders().size());
        
        return result;
    }
    
    /**
     * Processa uma ordem individual de forma thread-safe
     * Verifica se a ordem já existe antes de criar uma nova
     * 
     * @param order Ordem a ser processada
     * @param product Produto associado
     * @param participant Participante associado
     * @param result Resultado compartilhado thread-safe
     */
    private void processIndividualOrder(TechOrdersResponse order, ProductEntity product, 
                                      ParticipantEntity participant, ProcessingResult result) {

        Optional<OrderEntity> existingOrder = orderRepository.findByOrderId(order.getOrderId());
        
        if (existingOrder.isPresent()) {
            logger.debug("Order {} already exists", order.getOrderId());
            result.addExistingOrder(order.getOrderId());
        } else {
            logger.debug("Creating new order {}", order.getOrderId());
            entityCreationService.createAndSaveOrder(order, product, participant);
            result.addNewOrder(order);
        }
    }
} 