package com.maal.certifiedbuilderapi.business.usecases;

import com.maal.certifiedbuilderapi.business.dto.DeleteProductResponse;
import com.maal.certifiedbuilderapi.business.exception.ProductNotFoundException;
import com.maal.certifiedbuilderapi.business.usecase.certificate.DeleteProduct;
import com.maal.certifiedbuilderapi.domain.entity.CertificateEntity;
import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import com.maal.certifiedbuilderapi.infrastructure.aws.s3.S3ClientCustomer;
import com.maal.certifiedbuilderapi.infrastructure.repository.CertificateRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProductTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private S3ClientCustomer s3ClientCustomer;

    @InjectMocks
    private DeleteProduct deleteProduct;

    @Test
    @DisplayName("Should delete product and all its associated data")
    void shouldDeleteProductAndAllItsAssociatedData() {
        // Given
        Integer productId = 1;
        ProductEntity product = new ProductEntity();
        OrderEntity order = new OrderEntity();
        CertificateEntity certificate = new CertificateEntity();

        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));
        when(orderRepository.findByProduct_ProductId(productId)).thenReturn(List.of(order));
        when(certificateRepository.findByOrder_Product_ProductId(productId)).thenReturn(List.of(certificate));
        // When
        DeleteProductResponse result = deleteProduct.execute(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertNotNull(result.getDeletedAt());

        // Verify
        verify(productRepository).deleteByProductId(productId);
        verify(orderRepository).deleteByProduct_ProductId(productId);
        verify(certificateRepository).deleteByOrder_Product_ProductId(productId);
        verify(s3ClientCustomer).deleteProductCertificatesDirectory(productId);
    }


    @Test
    @DisplayName("Should throw ProductNotFoundException when product does not exist")
    void shouldThrowProductNotFoundExceptionWhenProductDoesNotExist(){
        // Given
        Integer productId = 1;

        when(productRepository.findByProductId(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> deleteProduct.execute(productId));

        // Verify
        verify(productRepository).findByProductId(productId);
        verifyNoMoreInteractions(orderRepository, certificateRepository, s3ClientCustomer);
    }

    @Test
    @DisplayName("Should delete product with no certificates and orders associated")
    void shouldDeleteProductWithNoCertificatesAndOrdersAssociated() {
        // Given
        Integer productId = 1;
        ProductEntity product = new ProductEntity();
        product.setProductId(productId);
        product.setTimeCheckin(LocalDateTime.now().toString());

        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));

        // When
        DeleteProductResponse result = deleteProduct.execute(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertNotNull(result.getDeletedAt());

        // Verify
        verify(productRepository).deleteByProductId(productId);
        verify(orderRepository).deleteByProduct_ProductId(productId);
        verify(certificateRepository).deleteByOrder_Product_ProductId(productId);
        verify(s3ClientCustomer).deleteProductCertificatesDirectory(productId);
    }
}