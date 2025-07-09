package com.maal.certifiedbuilderapi.business.usecase.certificate;

import com.maal.certifiedbuilderapi.business.dto.BuildOrdersRequest;
import com.maal.certifiedbuilderapi.business.dto.BuildOrdersResponse;
import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import com.maal.certifiedbuilderapi.domain.entity.ParticipantEntity;
import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import com.maal.certifiedbuilderapi.infrastructure.aws.sqs.OrderEventPublisher;
import com.maal.certifiedbuilderapi.infrastructure.client.TechFloripa;
import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ParticipantRespository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;


/**
 * Use case para construção de ordens de certificados
 * Adaptado para trabalhar com dados desnormalizados do DynamoDB
 * Otimizado para processamento paralelo e melhor performance
 */
@Service
@RequiredArgsConstructor
public class CertificateConstructionOrder {

    private static final Logger logger = LoggerFactory.getLogger(CertificateConstructionOrder.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TechFloripa techFloripa;
    private final OrderRepository orderRepository;
    private final ParticipantRespository participantRespository;
    private final ProductRepository productRepository;
    private final OrderEventPublisher orderEventPublisher;

    /**
     * Classe auxiliar para armazenar resultados do processamento paralelo
     */
    private static class ProcessingResult {
        private final List<Integer> existingOrders = new CopyOnWriteArrayList<>();
        private final List<TechOrdersResponse> newOrders = new CopyOnWriteArrayList<>();
        
        public void addExistingOrder(Integer orderId) {
            existingOrders.add(orderId);
        }
        
        public void addNewOrder(TechOrdersResponse order) {
            newOrders.add(order);
        }
        
        public List<Integer> getExistingOrders() {
            return new ArrayList<>(existingOrders);
        }
        
        public List<TechOrdersResponse> getNewOrders() {
            return new ArrayList<>(newOrders);
        }
    }

    /**
     * Processes orders from TechFloripa and creates certificates for new orders.
     *
     * @param orders The list of TechOrdersResponse containing the product ID
     * @return BuildOrdersResponse with processing results
     */
    public BuildOrdersResponse execute(List<TechOrdersResponse> orders) {
        logger.info("Executing construction order");

        if (CollectionUtils.isEmpty(orders)) {
            logger.warn("No orders found");
            return BuildOrdersResponse.builder()
                    .certificateQuantity(0)
                    .existingOrders(List.of())
                    .newOrders(List.of())
                    .build();
        }
        
        ProcessingResult result = processOrdersParallel(orders);
        publishNewOrders(result.getNewOrders());

        return buildResponse(result.getExistingOrders(), result.getNewOrders());
    }
    
    /**
     * Processes orders from TechFloripa and creates certificates for new orders.
     *
     * @param request The build orders request containing the product ID
     * @return BuildOrdersResponse with processing results
     */
    public BuildOrdersResponse execute(BuildOrdersRequest request) {
        List<TechOrdersResponse> orders = techFloripa.getOrders(request.getProductId());
        
        if (CollectionUtils.isEmpty(orders)) {
            return BuildOrdersResponse.builder()
                    .certificateQuantity(0)
                    .existingOrders(List.of())
                    .newOrders(List.of())
                    .build();
        }
        
        ProcessingResult result = processOrdersParallel(orders);
        publishNewOrders(result.getNewOrders());

        return buildResponse(result.getExistingOrders(), result.getNewOrders());
    }

    private ProductEntity getOrCreateProduct(TechOrdersResponse sampleOrder) {
        return productRepository.findByProductId(sampleOrder.getProductId())
                .orElseGet(() -> createAndSaveProduct(sampleOrder));
    }

    private ProductEntity createAndSaveProduct(TechOrdersResponse order) {
        ProductEntity newProduct = new ProductEntity();
        newProduct.setProductId(order.getProductId());
        newProduct.setProductName(order.getProductName());
        newProduct.setCertificateDetails(order.getCertificateDetails());
        newProduct.setCertificateLogo(order.getCertificateLogo());
        newProduct.setCertificateBackground(order.getCertificateBackground());
        newProduct.setCheckinLatitude(order.getCheckinLatitude());
        newProduct.setCheckinLongitude(order.getCheckinLongitude());
        newProduct.setTimeCheckin(order.getTimeCheckin());
        return productRepository.save(newProduct);
    }

    /**
     * Processa ordens de forma paralela para melhor performance
     * Utiliza streams paralelos e cache local para otimizar consultas ao banco
     * 
     * @param orders Lista de ordens a serem processadas
     * @return ProcessingResult com ordens existentes e novas
     */
    private ProcessingResult processOrdersParallel(List<TechOrdersResponse> orders) {
        logger.info("Processing {} orders using parallel streams", orders.size());
        
        // Cache local para produtos e participantes para evitar consultas repetidas
        ConcurrentHashMap<Integer, ProductEntity> productCache = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, ParticipantEntity> participantCache = new ConcurrentHashMap<>();
        
        ProcessingResult result = new ProcessingResult();
        
        // Filtra ordens válidas (com timeCheckin não vazio) e processa em paralelo
        orders.parallelStream()
            .filter(order -> !order.getTimeCheckin().isEmpty())
            .forEach(order -> {
                try {
                    // Busca ou cria produto usando cache local para evitar consultas duplicadas
                    ProductEntity product = productCache.computeIfAbsent(
                        order.getProductId(), 
                        k -> getOrCreateProduct(order)
                    );
                    
                    // Busca ou cria participante usando cache local
                    ParticipantEntity participant = participantCache.computeIfAbsent(
                        order.getEmail(),
                        k -> getOrCreateParticipant(order)
                    );
                    
                    // Processa a ordem individual
                    processOrderParallel(order, product, participant, result);
                    
                } catch (Exception e) {
                    logger.error("Erro ao processar ordem {}: {}", order.getOrderId(), e.getMessage(), e);
                }
            });
        
        logger.info("Finished processing orders - New: {}, Existing: {}", 
                   result.getNewOrders().size(), result.getExistingOrders().size());
        
        return result;
    }

    private ParticipantEntity getOrCreateParticipant(TechOrdersResponse order) {
        return participantRespository.findByEmail(order.getEmail())
                .orElseGet(() -> createAndSaveParticipant(order));
    }

    private ParticipantEntity createAndSaveParticipant(TechOrdersResponse order) {
        ParticipantEntity newParticipant = new ParticipantEntity();
        newParticipant.setFirstName(order.getFirstName());
        newParticipant.setLastName(order.getLastName());
        newParticipant.setEmail(order.getEmail());
        newParticipant.setPhone(order.getPhone());
        newParticipant.setCpf(order.getCpf());
        newParticipant.setCity(order.getCity());
        return participantRespository.save(newParticipant);
    }

    /**
     * Processa uma ordem individual de forma thread-safe
     * Versão otimizada para processamento paralelo
     * 
     * @param order Ordem a ser processada
     * @param product Produto associado à ordem
     * @param participant Participante associado à ordem
     * @param result Resultado compartilhado thread-safe para coletar dados
     */
    private void processOrderParallel(TechOrdersResponse order, ProductEntity product, 
                                    ParticipantEntity participant, ProcessingResult result) {

        Optional<OrderEntity> existingOrder = orderRepository.findByOrderId(order.getOrderId());
        
        if (existingOrder.isPresent()) {
            logger.debug("Order {} already exists", order.getOrderId());
            result.addExistingOrder(order.getOrderId());
        } else {
            logger.debug("Creating new order {}", order.getOrderId());
            createAndSaveOrder(order, product, participant);
            result.addNewOrder(order);
        }
    }

    /**
     * Cria e salva uma nova ordem com dados desnormalizados
     * Agora preenche campos individuais em vez de setar objetos aninhados
     */
    private void createAndSaveOrder(TechOrdersResponse order, ProductEntity product, ParticipantEntity participant) {
        OrderEntity newOrder = new OrderEntity();
        
        // === DADOS BÁSICOS DO PEDIDO ===
        newOrder.setOrderId(order.getOrderId());
        // Usa método utilitário para converter LocalDateTime para String compatível com DynamoDB
        newOrder.setOrderDateFromLocalDateTime(LocalDateTime.parse(order.getOrderDate(), DATE_FORMATTER));
        
        // === DADOS DESNORMALIZADOS DO PRODUTO ===
        newOrder.setProductId(product.getProductId());
        newOrder.setProductName(product.getProductName());
        newOrder.setCertificateDetails(product.getCertificateDetails());
        newOrder.setCertificateLogo(product.getCertificateLogo());
        newOrder.setCertificateBackground(product.getCertificateBackground());
        newOrder.setCheckinLatitude(product.getCheckinLatitude());
        newOrder.setCheckinLongitude(product.getCheckinLongitude());
        newOrder.setTimeCheckin(product.getTimeCheckin());
        
        // === DADOS DESNORMALIZADOS DO PARTICIPANTE ===
        newOrder.setParticipantEmail(participant.getEmail());
        newOrder.setParticipantFirstName(participant.getFirstName());
        newOrder.setParticipantLastName(participant.getLastName());
        newOrder.setParticipantCpf(participant.getCpf());
        newOrder.setParticipantPhone(participant.getPhone());
        newOrder.setParticipantCity(participant.getCity());
        
        orderRepository.save(newOrder);
    }

    private void publishNewOrders(List<TechOrdersResponse> newOrders) {
        if (CollectionUtils.isEmpty(newOrders)) {
            logger.warn("No new orders found");
            return;
        }
        logger.info("Publishing new orders {}", newOrders.size());
        orderEventPublisher.publishOrderCreatedEvent(newOrders);
    }

    private BuildOrdersResponse buildResponse(List<Integer> existingOrders,
                                            List<TechOrdersResponse> newOrders) {
        return BuildOrdersResponse.builder()
                .certificateQuantity(newOrders.size())
                .existingOrders(existingOrders)
                .newOrders(newOrders.stream().map(TechOrdersResponse::getOrderId).toList())
                .build();
    }
}
