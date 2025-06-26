package com.maal.certifiedbuilderapi.business.usecases;

import com.maal.certifiedbuilderapi.business.dto.CertificateStatisticsResponse;
import com.maal.certifiedbuilderapi.business.usecase.certificate.GetCertificateStatistics;
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
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Testes para GetCertificateStatistics
 * Atualizados para usar métodos desnormalizados do DynamoDB
 */
@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
class GetCertificateStatisticsTests {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private GetCertificateStatistics getCertificateStatistics;

    @Test
    @DisplayName("Should return correct statistics when certificates exist")
    void shouldReturnCorrectStatisticsWhenCertificatesExist() {
        // Given
        Integer productId = 1;
        ProductEntity product = new ProductEntity();
        product.setProductId(productId);
        product.setProductName("Test Product");

        CertificateEntity cert1 = new CertificateEntity();
        cert1.setSuccess(true);
        CertificateEntity cert2 = new CertificateEntity();
        cert2.setSuccess(false);
        CertificateEntity cert3 = new CertificateEntity();
        cert3.setSuccess(null);

        OrderEntity order1 = new OrderEntity();
        OrderEntity order2 = new OrderEntity();

        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));
        when(certificateRepository.findByProductId(productId)).thenReturn(Arrays.asList(cert1, cert2, cert3));
        when(orderRepository.findByProductId(productId)).thenReturn(Arrays.asList(order1, order2));

        // When
        CertificateStatisticsResponse result = getCertificateStatistics.execute(productId);

        // Then
        assertEquals(productId, result.getProductId());
        assertEquals("Test Product", result.getProductName());
        assertEquals(2, result.getTotalCertificates());
        assertEquals(1, result.getSuccessfulCertificates());
        assertEquals(1, result.getFailedCertificates());
        assertEquals(1, result.getPendingCertificates());
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void shouldThrowExceptionWhenProductNotFound() {
        // Given
        Integer productId = 99;
        when(productRepository.findByProductId(productId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getCertificateStatistics.execute(productId)
        );
        assertTrue(exception.getMessage().contains("Product not found"));
    }

    @Test
    @DisplayName("Should return zero statistics when no certificates exist")
    void shouldReturnZeroStatisticsWhenNoCertificatesExist() {
        // Given
        Integer productId = 1;
        ProductEntity product = new ProductEntity();
        product.setProductId(productId);
        product.setProductName("Empty Product");

        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));
        when(certificateRepository.findByProductId(productId)).thenReturn(List.of());
        when(orderRepository.findByProductId(productId)).thenReturn(List.of());

        // When
        CertificateStatisticsResponse result = getCertificateStatistics.execute(productId);

        // Then
        assertEquals(productId, result.getProductId());
        assertEquals("Empty Product", result.getProductName());
        assertEquals(0, result.getTotalCertificates());
        assertEquals(0, result.getSuccessfulCertificates());
        assertEquals(0, result.getFailedCertificates());
        assertEquals(0, result.getPendingCertificates());
    }

    @Test
    @DisplayName("Should handle only successful certificates")
    void shouldHandleOnlySuccessfulCertificates() {
        // Given
        Integer productId = 1;
        ProductEntity product = new ProductEntity();
        product.setProductId(productId);
        product.setProductName("Success Product");

        CertificateEntity cert1 = new CertificateEntity();
        cert1.setSuccess(true);
        CertificateEntity cert2 = new CertificateEntity();
        cert2.setSuccess(true);

        OrderEntity order1 = new OrderEntity();

        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));
        when(certificateRepository.findByProductId(productId)).thenReturn(Arrays.asList(cert1, cert2));
        when(orderRepository.findByProductId(productId)).thenReturn(Arrays.asList(order1));

        // When
        CertificateStatisticsResponse result = getCertificateStatistics.execute(productId);

        // Then
        assertEquals(1, result.getTotalCertificates());
        assertEquals(2, result.getSuccessfulCertificates());
        assertEquals(0, result.getFailedCertificates());
        assertEquals(0, result.getPendingCertificates());
    }
}
