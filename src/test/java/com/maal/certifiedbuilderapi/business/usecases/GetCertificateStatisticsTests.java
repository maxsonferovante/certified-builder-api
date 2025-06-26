package com.maal.certifiedbuilderapi.business.usecases;

import com.maal.certifiedbuilderapi.business.dto.CertificateStatisticsResponse;
import com.maal.certifiedbuilderapi.business.exception.ProductNotFoundException;
import com.maal.certifiedbuilderapi.business.usecase.certificate.GetCertificateStatistics;
import com.maal.certifiedbuilderapi.config.AwsTestConfig;
import com.maal.certifiedbuilderapi.config.FeignConfig;
import com.maal.certifiedbuilderapi.config.TestConfig;
import com.maal.certifiedbuilderapi.domain.entity.CertificateEntity;
import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import com.maal.certifiedbuilderapi.infrastructure.repository.CertificateRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes para GetCertificateStatistics
 * Atualizados para usar métodos desnormalizados do DynamoDB
 */
@SpringBootTest
@Import({TestConfig.class, FeignConfig.class, AwsTestConfig.class})
@ActiveProfiles("test")
class GetCertificateStatisticsTests {

    @Autowired
    private GetCertificateStatistics getCertificateStatistics;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Test
    @DisplayName("Should return correct statistics when certificates exist")
    void shouldReturnCorrectStatisticsWhenCertificatesExist() {
        // Given
        Integer productId = 1;
        ProductEntity product = new ProductEntity();
        product.setProductId(productId);
        product.setProductName("Test Product");

        OrderEntity order1 = new OrderEntity();
        order1.setOrderId(1);
        order1.setProductId(productId);

        OrderEntity order2 = new OrderEntity();
        order2.setOrderId(2);
        order2.setProductId(productId);

        CertificateEntity certificate1 = new CertificateEntity();
        certificate1.setId(UUID.randomUUID().toString());
        certificate1.setOrderId(order1.getOrderId());
        certificate1.setSuccess(true);

        CertificateEntity certificate2 = new CertificateEntity();
        certificate2.setId(UUID.randomUUID().toString());
        certificate2.setOrderId(order2.getOrderId());
        certificate2.setSuccess(false);

        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));
        when(orderRepository.findByProductId(productId)).thenReturn(List.of(order1, order2));
        when(certificateRepository.findByOrderId(order1.getOrderId())).thenReturn(Optional.of(certificate1));
        when(certificateRepository.findByOrderId(order2.getOrderId())).thenReturn(Optional.of(certificate2));

        // When
        CertificateStatisticsResponse response = getCertificateStatistics.execute(productId);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getTotalCertificates());
        assertEquals(1, response.getSuccessfulCertificates());
        assertEquals(1, response.getFailedCertificates());
        assertEquals(0, response.getPendingCertificates());
    }

    @Test
    @DisplayName("Should return zero statistics when no certificates exist")
    void shouldReturnZeroStatisticsWhenNoCertificatesExist() {
        // Given
        Integer productId = 1;
        ProductEntity product = new ProductEntity();
        product.setProductId(productId);
        product.setProductName("Test Product");

        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));
        when(orderRepository.findByProductId(productId)).thenReturn(List.of());

        // When
        CertificateStatisticsResponse response = getCertificateStatistics.execute(productId);

        // Then
        assertNotNull(response);
        assertEquals(0, response.getTotalCertificates());
        assertEquals(0, response.getSuccessfulCertificates());
        assertEquals(0, response.getFailedCertificates());
        assertEquals(0, response.getPendingCertificates());
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void shouldThrowExceptionWhenProductNotFound() {
        // Given
        Integer productId = 99;
        when(productRepository.findByProductId(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> getCertificateStatistics.execute(productId));
    }

    @Test
    @DisplayName("Should handle only successful certificates")
    void shouldHandleOnlySuccessfulCertificates() {
        // Given
        Integer productId = 1;
        ProductEntity product = new ProductEntity();
        product.setProductId(productId);
        product.setProductName("Test Product");

        OrderEntity order1 = new OrderEntity();
        order1.setOrderId(1);
        order1.setProductId(productId);

        OrderEntity order2 = new OrderEntity();
        order2.setOrderId(2);
        order2.setProductId(productId);

        CertificateEntity certificate1 = new CertificateEntity();
        certificate1.setId(UUID.randomUUID().toString());
        certificate1.setOrderId(order1.getOrderId());
        certificate1.setSuccess(true);

        CertificateEntity certificate2 = new CertificateEntity();
        certificate2.setId(UUID.randomUUID().toString());
        certificate2.setOrderId(order2.getOrderId());
        certificate2.setSuccess(true);

        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));
        when(orderRepository.findByProductId(productId)).thenReturn(List.of(order1, order2));
        when(certificateRepository.findByOrderId(order1.getOrderId())).thenReturn(Optional.of(certificate1));
        when(certificateRepository.findByOrderId(order2.getOrderId())).thenReturn(Optional.of(certificate2));

        // When
        CertificateStatisticsResponse response = getCertificateStatistics.execute(productId);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getTotalCertificates());
        assertEquals(2, response.getSuccessfulCertificates());
        assertEquals(0, response.getFailedCertificates());
        assertEquals(0, response.getPendingCertificates());
    }
}
