package com.maal.certifiedbuilderapi.business.usecases;


import com.maal.certifiedbuilderapi.business.dto.DeleteProductResponse;
import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import com.maal.certifiedbuilderapi.business.exception.ProductNotFoundException;
import com.maal.certifiedbuilderapi.infrastructure.repository.CertificateRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRespository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeleteProduct {

    private final OrderRespository orderRespository;
    private final ProductRepository productRepository;
    private final CertificateRepository certificateRepository;

    public DeleteProductResponse execute(Integer productId) {
        // Verifica se o produto existe
        ProductEntity product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException("Produto com ID " + productId + " não encontrado."));

        productRepository.deleteByProductId(productId);
        orderRespository.deleteByProduct_ProductId(productId);
        certificateRepository.deleteByOrder_Product_ProductId(productId);

        return DeleteProductResponse
                .builder()
                .productId(productId)
                .deletedAt(LocalDateTime.now())
                .build();

    }

}
