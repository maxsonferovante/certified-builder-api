package com.maal.certifiedbuilderapi.business.usecase.certificate.construction;

import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import com.maal.certifiedbuilderapi.domain.entity.ParticipantEntity;
import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ParticipantRespository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Serviço responsável pela criação e persistência de entidades
 * Centraliza lógica de criação de Product, Participant e Order
 */
@Service
@RequiredArgsConstructor
public class EntityCreationService {
    
    private static final Logger logger = LoggerFactory.getLogger(EntityCreationService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final ProductRepository productRepository;
    private final ParticipantRespository participantRespository;
    private final OrderRepository orderRepository;
    
    /**
     * Cria e salva um novo produto no banco de dados
     * 
     * @param order Ordem contendo dados do produto
     * @return Produto criado e salvo
     */
    public ProductEntity createAndSaveProduct(TechOrdersResponse order) {
        logger.debug("Criando novo produto para productId: {}", order.getProductId());
        
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
     * Cria e salva um novo participante no banco de dados
     * 
     * @param order Ordem contendo dados do participante
     * @return Participante criado e salvo
     */
    public ParticipantEntity createAndSaveParticipant(TechOrdersResponse order) {
        logger.debug("Criando novo participante para email: {}", order.getEmail());
        
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
     * Cria e salva uma nova ordem com dados desnormalizados
     * Preenche campos individuais em vez de setar objetos aninhados para DynamoDB
     * 
     * @param order Ordem original da TechFloripa
     * @param product Produto associado
     * @param participant Participante associado
     * @return Ordem criada e salva
     */
    public OrderEntity createAndSaveOrder(TechOrdersResponse order, ProductEntity product, ParticipantEntity participant) {
        logger.debug("Criando nova ordem para orderId: {}", order.getOrderId());
        
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
        
        return orderRepository.save(newOrder);
    }
} 