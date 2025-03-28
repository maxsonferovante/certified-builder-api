package com.maal.certifiedbuilderapi.infrastructure.repository;

import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ProductRepository extends MongoRepository<ProductEntity, String> {
    Optional<ProductEntity> findByProductId(Integer productId);
    void deleteByProductId(Integer productId);
}
