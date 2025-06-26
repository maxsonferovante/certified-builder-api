package com.maal.certifiedbuilderapi.business.usecases;

import com.maal.certifiedbuilderapi.business.dto.DeleteProductResponse;
import com.maal.certifiedbuilderapi.business.exception.ProductNotFoundException;
import com.maal.certifiedbuilderapi.business.usecase.certificate.DeleteProduct;
import com.maal.certifiedbuilderapi.config.TestConfig;
import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import com.maal.certifiedbuilderapi.infrastructure.repository.CertificateRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes para DeleteProduct
 * Atualizados para usar métodos desnormalizados do DynamoDB
 */
@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
class DeleteProductTests {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private DeleteProduct deleteProduct;

    @Test
    @DisplayName("Should delete product and related data successfully")
    void shouldDeleteProductAndRelatedDataSuccessfully() {
        // Given
        Integer productId = 1;
        ProductEntity product = new ProductEntity();
        product.setProductId(productId);
        product.setProductName("Test Product");

        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).deleteByProductId(productId);
        doNothing().when(orderRepository).deleteByProductId(productId);
        doNothing().when(certificateRepository).deleteByProductId(productId);

        // When
        DeleteProductResponse result = deleteProduct.execute(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertNotNull(result.getDeletedAt());

        // Verify interactions
        verify(productRepository).findByProductId(productId);
        verify(productRepository).deleteByProductId(productId);
        verify(orderRepository).deleteByProductId(productId);
        verify(certificateRepository).deleteByProductId(productId);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when product not found")
    void shouldThrowProductNotFoundExceptionWhenProductNotFound() {
        // Given
        Integer productId = 99;
        when(productRepository.findByProductId(productId)).thenReturn(Optional.empty());

        // When & Then
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> deleteProduct.execute(productId)
        );
        assertEquals("Product not found with ID: " + productId, exception.getMessage());

        // Verify no deletion operations were called
        verify(productRepository, never()).deleteByProductId(any());
        verify(orderRepository, never()).deleteByProductId(any());
        verify(certificateRepository, never()).deleteByProductId(any());
    }

    @Test
    @DisplayName("Should handle deletion when no related data exists")
    void shouldHandleDeletionWhenNoRelatedDataExists() {
        // Given
        Integer productId = 1;
        ProductEntity product = new ProductEntity();
        product.setProductId(productId);
        product.setProductName("Empty Product");

        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).deleteByProductId(productId);
        doNothing().when(orderRepository).deleteByProductId(productId);
        doNothing().when(certificateRepository).deleteByProductId(productId);

        // When
        DeleteProductResponse result = deleteProduct.execute(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertNotNull(result.getDeletedAt());

        // Verify all deletion operations were called even with no data
        verify(productRepository).deleteByProductId(productId);
        verify(orderRepository).deleteByProductId(productId);
        verify(certificateRepository).deleteByProductId(productId);
    }
}