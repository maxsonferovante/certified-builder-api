package com.maal.certifiedbuilderapi.business.usecases;

import com.maal.certifiedbuilderapi.business.dto.DeleteProductResponse;
import com.maal.certifiedbuilderapi.business.exception.ProductNotFoundException;
import com.maal.certifiedbuilderapi.business.usecase.certificate.DeleteProduct;
import com.maal.certifiedbuilderapi.config.FeignConfig;
import com.maal.certifiedbuilderapi.config.TestConfig;
import com.maal.certifiedbuilderapi.domain.entity.CertificateEntity;
import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import com.maal.certifiedbuilderapi.infrastructure.aws.s3.S3ClientCustomer;
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
 * Testes para DeleteProduct
 * Atualizados para usar métodos desnormalizados do DynamoDB
 */
@SpringBootTest
@Import({TestConfig.class, FeignConfig.class})
@ActiveProfiles("test")
class DeleteProductTests {

    @Autowired
    private DeleteProduct deleteProduct;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private S3ClientCustomer s3ClientCustomer;

    @Test
    @DisplayName("Should delete product and related data successfully")
    void deleteProductAndRelatedDataSuccessfully() {
        // Given
        Integer productId = 1;
        ProductEntity product = new ProductEntity();
        product.setProductId(productId);

        OrderEntity order = new OrderEntity();
        order.setOrderId(1);
        order.setProductId(productId);

        CertificateEntity certificate = new CertificateEntity();
        certificate.setId(UUID.randomUUID().toString());
        certificate.setOrderId(order.getOrderId());

        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));
        when(orderRepository.findByProductId(productId)).thenReturn(List.of(order));
        when(certificateRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(certificate));

        // When
        DeleteProductResponse response = deleteProduct.execute(productId);

        // Then
        assertNotNull(response);
        assertEquals(productId, response.getProductId());
        assertNotNull(response.getDeletedAt());

        verify(certificateRepository, times(1)).deleteById(certificate.getId());
        verify(orderRepository, times(1)).deleteById(order.getOrderId().toString());
        verify(productRepository, times(1)).deleteById(productId.toString());
        verify(s3ClientCustomer, times(1)).deleteCertificate(certificate.getId());
    }

    @Test
    @DisplayName("Should handle deletion when no related data exists")
    void handleDeletionWhenNoRelatedDataExists() {
        // Given
        Integer productId = 1;
        ProductEntity product = new ProductEntity();
        product.setProductId(productId);

        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));
        when(orderRepository.findByProductId(productId)).thenReturn(List.of());

        // When
        DeleteProductResponse response = deleteProduct.execute(productId);

        // Then
        assertNotNull(response);
        assertEquals(productId, response.getProductId());
        assertNotNull(response.getDeletedAt());

        verify(certificateRepository, never()).deleteById(any());
        verify(orderRepository, never()).deleteById(any());
        verify(productRepository, times(1)).deleteById(productId.toString());
        verify(s3ClientCustomer, never()).deleteCertificate(any());
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when product not found")
    void throwProductNotFoundExceptionWhenProductNotFound() {
        // Given
        Integer productId = 1;
        when(productRepository.findByProductId(productId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ProductNotFoundException.class, () -> deleteProduct.execute(productId));

        verify(certificateRepository, never()).deleteById(any());
        verify(orderRepository, never()).deleteById(any());
        verify(productRepository, never()).deleteById(any());
        verify(s3ClientCustomer, never()).deleteCertificate(any());
    }
}