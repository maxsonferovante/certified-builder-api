package com.maal.certifiedbuilderapi.infrastructure.repository;

import com.maal.certifiedbuilderapi.domain.entity.CertificateEntity;
import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends MongoRepository<CertificateEntity, String> {
    Optional<CertificateEntity> findByOrder(OrderEntity order);
    void deleteByOrder_Product_ProductId(Integer productId);
    List<CertificateEntity> findByOrder_Product_ProductId(Integer productId);
}
