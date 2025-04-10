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

@Service
@RequiredArgsConstructor
public class GetCertificateStatistics {

    private final OrderRepository orderRepository;
    private final CertificateRepository certificateRepository;
    private final ProductRepository productRepository;

    /**
     * Gets statistics about certificate processing for a specific product.
     *
     * @param productId The ID of the product to get statistics for
     * @return CertificateStatisticsResponse containing the statistics
     */
    @Transactional(readOnly = true)
    public CertificateStatisticsResponse execute(Integer productId) {
        ProductEntity product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        List<CertificateEntity> certificates = certificateRepository.findByOrder_Product_ProductId(productId);
        List<OrderEntity> orderEntities = orderRepository.findByProduct_ProductId(productId);

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