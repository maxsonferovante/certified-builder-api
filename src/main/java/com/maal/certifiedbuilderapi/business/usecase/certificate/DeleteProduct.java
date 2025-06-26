package com.maal.certifiedbuilderapi.business.usecase.certificate;

import com.maal.certifiedbuilderapi.business.dto.DeleteProductResponse;
import com.maal.certifiedbuilderapi.business.exception.ProductNotFoundException;
import com.maal.certifiedbuilderapi.domain.entity.CertificateEntity;
import com.maal.certifiedbuilderapi.infrastructure.aws.s3.S3ClientCustomer;
import com.maal.certifiedbuilderapi.infrastructure.repository.CertificateRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Use case responsible for deleting a product and all its associated data.
 * Adaptado para trabalhar com dados desnormalizados do DynamoDB
 * This includes:
 * - The product itself
 * - All associated orders
 * - All certificates stored in S3
 */
@Service
@RequiredArgsConstructor
public class DeleteProduct {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CertificateRepository certificateRepository;
    private final S3ClientCustomer s3ClientCustomer;

    /**
     * Deletes a product and all its associated data.
     *
     * @param productId The ID of the product to delete
     * @return DeleteProductResponse containing the deletion details
     * @throws ProductNotFoundException if the product is not found
     */
    @Transactional
    public DeleteProductResponse execute(Integer productId) {
        validateProductExists(productId);
        
        deleteProductAndRelatedData(productId);
        
        return buildResponse(productId);
    }

    /**
     * Validates if the product exists before attempting deletion.
     *
     * @param productId The ID of the product to validate
     * @throws ProductNotFoundException if the product is not found
     */
    private void validateProductExists(Integer productId) {
        productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                    String.format("Product with ID %d not found.", productId)));
    }

    /**
     * Deletes the product and all its related data in the correct order.
     *
     * @param productId The ID of the product to delete
     */
    private void deleteProductAndRelatedData(Integer productId) {
        deleteProduct(productId);
        deleteOrders(productId);
        deleteCertificates(productId);
    }

    /**
     * Deletes the product from the database.
     *
     * @param productId The ID of the product to delete
     */
    private void deleteProduct(Integer productId) {
        productRepository.deleteByProductId(productId);
    }

    /**
     * Deletes all orders associated with the product.
     * Agora usa dados desnormalizados - busca direta por productId
     *
     * @param productId The ID of the product whose orders should be deleted
     */
    private void deleteOrders(Integer productId) {
        // Método atualizado para DynamoDB - busca direta por productId desnormalizado
        orderRepository.deleteByProductId(productId);
    }

    /**
     * Deletes all certificates associated with the product from both S3 and the database.
     * Agora usa dados desnormalizados - busca direta por productId
     *
     * @param productId The ID of the product whose certificates should be deleted
     */
    private void deleteCertificates(Integer productId) {
        // Deleta todos os certificados do diretório no S3
        s3ClientCustomer.deleteProductCertificatesDirectory(productId);
        
        // Método atualizado para DynamoDB - busca direta por productId desnormalizado
        certificateRepository.deleteByProductId(productId);
    }

    /**
     * Builds the response object with the deletion details.
     *
     * @param productId The ID of the deleted product
     * @return DeleteProductResponse containing the deletion details
     */
    private DeleteProductResponse buildResponse(Integer productId) {
        return DeleteProductResponse.builder()
                .productId(productId)
                .deletedAt(LocalDateTime.now())
                .build();
    }
}
