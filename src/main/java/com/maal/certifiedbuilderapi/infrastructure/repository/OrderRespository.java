package com.maal.certifiedbuilderapi.infrastructure.repository;

import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface OrderRespository  extends MongoRepository<OrderEntity, String> {
    Optional<OrderEntity> findByOrderId(Integer orderId);

    List<OrderEntity> findByProduct(ProductEntity product);

    void deleteByProduct_ProductId(Integer productId);
}
