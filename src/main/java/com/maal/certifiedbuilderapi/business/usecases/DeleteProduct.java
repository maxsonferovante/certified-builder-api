package com.maal.certifiedbuilderapi.business.usecases;


import com.maal.certifiedbuilderapi.business.dto.DeleteProductResponse;
import com.maal.certifiedbuilderapi.domain.entity.CertificateEntity;
import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import com.maal.certifiedbuilderapi.business.exception.ProductNotFoundException;
import com.maal.certifiedbuilderapi.infrastructure.aws.s3.S3ClientCustomer;
import com.maal.certifiedbuilderapi.infrastructure.repository.CertificateRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRespository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeleteProduct {

    private final OrderRespository orderRespository;
    private final ProductRepository productRepository;
    private final CertificateRepository certificateRepository;
    private final S3ClientCustomer s3ClientCustomer;

    public DeleteProductResponse execute(Integer productId) {
        // Verifica se o produto existe
        ProductEntity product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException("Produto com ID " + productId + " não encontrado."));

        productRepository.deleteByProductId(productId);
        orderRespository.deleteByProduct_ProductId(productId);
        deleteCertificatesInS3(productId);

        return DeleteProductResponse
                .builder()
                .productId(productId)
                .deletedAt(LocalDateTime.now())
                .build();

    }
    private void deleteCertificatesInS3(Integer productId) {
        certificateRepository.findByOrder_Product_ProductId(productId).forEach(certificate -> {
            s3ClientCustomer.deleteCertificate(
                    certificate.getCertificateKey()
            );
        });
        certificateRepository.deleteByOrder_Product_ProductId(productId);

    }

}
