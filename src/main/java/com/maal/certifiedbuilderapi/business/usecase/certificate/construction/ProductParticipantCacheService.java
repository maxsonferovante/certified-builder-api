package com.maal.certifiedbuilderapi.business.usecase.certificate.construction;

import com.maal.certifiedbuilderapi.domain.entity.ParticipantEntity;
import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;
import com.maal.certifiedbuilderapi.infrastructure.repository.ParticipantRespository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço responsável por gerenciar cache local de produtos e participantes
 * Otimiza performance evitando consultas repetidas ao banco durante processamento paralelo
 */
@Service
@RequiredArgsConstructor
public class ProductParticipantCacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductParticipantCacheService.class);
    
    private final ProductRepository productRepository;
    private final ParticipantRespository participantRespository;
    private final EntityCreationService entityCreationService;
    
    /**
     * Busca ou cria produto usando cache local thread-safe
     * Evita consultas duplicadas para o mesmo productId durante processamento paralelo
     * 
     * @param order Ordem contendo dados do produto
     * @param productCache Cache local de produtos
     * @return Produto encontrado ou criado
     */
    public ProductEntity getOrCreateProductWithCache(TechOrdersResponse order, 
                                                   ConcurrentHashMap<Integer, ProductEntity> productCache) {
        return productCache.computeIfAbsent(order.getProductId(), productId -> {
            logger.debug("Buscando produto {} no banco (não encontrado no cache)", productId);
            return getOrCreateProduct(order);
        });
    }
    
    /**
     * Busca ou cria participante usando cache local thread-safe
     * Evita consultas duplicadas para o mesmo email durante processamento paralelo
     * 
     * @param order Ordem contendo dados do participante
     * @param participantCache Cache local de participantes
     * @return Participante encontrado ou criado
     */
    public ParticipantEntity getOrCreateParticipantWithCache(TechOrdersResponse order,
                                                          ConcurrentHashMap<String, ParticipantEntity> participantCache) {
        return participantCache.computeIfAbsent(order.getEmail(), email -> {
            logger.debug("Buscando participante {} no banco (não encontrado no cache)", email);
            return getOrCreateParticipant(order);
        });
    }
    
    /**
     * Cria caches thread-safe para nova sessão de processamento
     * 
     * @return Array com [productCache, participantCache]
     */
    public Object[] createCaches() {
        return new Object[]{
            new ConcurrentHashMap<Integer, ProductEntity>(),
            new ConcurrentHashMap<String, ParticipantEntity>()
        };
    }
    
    /**
     * Busca produto no repositório ou cria novo se não existir
     * 
     * @param order Ordem contendo dados do produto
     * @return Produto encontrado ou criado
     */
    private ProductEntity getOrCreateProduct(TechOrdersResponse order) {
        return productRepository.findByProductId(order.getProductId())
                .orElseGet(() -> {
                    logger.debug("Criando novo produto {}", order.getProductId());
                    return entityCreationService.createAndSaveProduct(order);
                });
    }
    
    /**
     * Busca participante no repositório ou cria novo se não existir
     * 
     * @param order Ordem contendo dados do participante
     * @return Participante encontrado ou criado
     */
    private ParticipantEntity getOrCreateParticipant(TechOrdersResponse order) {
        return participantRespository.findByEmail(order.getEmail())
                .orElseGet(() -> {
                    logger.debug("Criando novo participante {}", order.getEmail());
                    return entityCreationService.createAndSaveParticipant(order);
                });
    }
} 