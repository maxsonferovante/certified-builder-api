package com.maal.certifiedbuilderapi.business.usecases;

import com.maal.certifiedbuilderapi.business.dto.CertificateStatisticsResponse;
import com.maal.certifiedbuilderapi.business.usecase.certificate.GetCertificateStatistics;
import com.maal.certifiedbuilderapi.domain.entity.CertificateEntity;
import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import com.maal.certifiedbuilderapi.infrastructure.repository.CertificateRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes para GetCertificateStatistics
 * Atualizados para usar métodos desnormalizados do DynamoDB
 */
@ExtendWith(MockitoExtension.class)
class GetCertificateStatisticsTests {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CertificateRepository certificateRepository;

    @InjectMocks
    private GetCertificateStatistics getCertificateStatistics;

    @Test
    @DisplayName("Should return statistics when product and certificates are present")
    void shouldReturnStatisticsWhenProductAndCertificatesArePresent() {
        // Given
        Integer productId = 1;
        ProductEntity product = new ProductEntity();
        product.setProductId(productId);
        product.setProductName("Example Product");

        OrderEntity order1 = new OrderEntity();
        OrderEntity order2 = new OrderEntity();

        CertificateEntity cert1 = new CertificateEntity(); // success = true
        cert1.setSuccess(true);
        CertificateEntity cert2 = new CertificateEntity(); // success = false
        cert2.setSuccess(false);
        CertificateEntity cert3 = new CertificateEntity(); // pending (null)
        cert3.setSuccess(null);

        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));
        when(orderRepository.findByProductId(productId)).thenReturn(List.of(order1, order2));
        when(certificateRepository.findByProductId(productId)).thenReturn(List.of(cert1, cert2, cert3));

        // When
        CertificateStatisticsResponse result = getCertificateStatistics.execute(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals("Example Product", result.getProductName());
        assertEquals(2, result.getTotalCertificates()); // Total orders
        assertEquals(1, result.getSuccessfulCertificates());
        assertEquals(1, result.getFailedCertificates());
        assertEquals(1, result.getPendingCertificates());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when product is not found")
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
    @DisplayName("Should return all counts as zero when no certificates or orders exist")
    void shouldReturnZeroStatsWhenNoData() {
        // Given
        Integer productId = 5;
        ProductEntity product = new ProductEntity();
        product.setProductId(productId);
        product.setProductName("Empty Product");

        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));
        when(orderRepository.findByProductId(productId)).thenReturn(List.of());
        when(certificateRepository.findByProductId(productId)).thenReturn(List.of());

        // When
        CertificateStatisticsResponse result = getCertificateStatistics.execute(productId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalCertificates());
        assertEquals(0, result.getSuccessfulCertificates());
        assertEquals(0, result.getFailedCertificates());
        assertEquals(0, result.getPendingCertificates());
    }
}
