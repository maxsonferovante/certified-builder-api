package com.maal.certifiedbuilderapi.business.usecase.certificate;

import com.maal.certifiedbuilderapi.business.dto.CertificateStatisticsResponse;
import com.maal.certifiedbuilderapi.domain.entity.CertificateEntity;
import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import com.maal.certifiedbuilderapi.infrastructure.repository.CertificateRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use case para obter estatísticas de certificados
 * Adaptado para trabalhar com dados desnormalizados do DynamoDB
 */
@Service
@RequiredArgsConstructor
public class GetCertificateStatistics {

    private final OrderRepository orderRepository;
    private final CertificateRepository certificateRepository;
    private final ProductRepository productRepository;

    /**
     * Gets statistics about certificate processing for a specific product.
     * Agora usa dados desnormalizados para melhor performance
     *
     * @param productId The ID of the product to get statistics for
     * @return CertificateStatisticsResponse containing the statistics
     */
    @Transactional(readOnly = true)
    public CertificateStatisticsResponse execute(Integer productId) {
        ProductEntity product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        // Usa métodos desnormalizados - busca direta por productId
        List<CertificateEntity> certificates = certificateRepository.findByProductId(productId);
        List<OrderEntity> orderEntities = orderRepository.findByProductId(productId);

        long successfulCount = certificates.stream()
                .filter(cert -> Boolean.TRUE.equals(cert.getSuccess()))
                .count();

        long failedCount = certificates.stream()
                .filter(cert -> Boolean.FALSE.equals(cert.getSuccess()))
                .count();

        long pendingCertificates = certificates.stream()
                .filter(cert -> cert.getSuccess() == null)
                .count();

        return CertificateStatisticsResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .totalCertificates(orderEntities.size())
                .successfulCertificates((int) successfulCount)
                .failedCertificates((int) failedCount)
                .pendingCertificates((int) pendingCertificates)
                .build();
    }
} 