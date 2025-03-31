package com.maal.certifiedbuilderapi.infrastructure.repository;

import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface OrderRepository extends MongoRepository<OrderEntity, String> {
    Optional<OrderEntity> findByOrderId(Integer orderId);
    void deleteByProduct_ProductId(Integer productId);
    List<OrderEntity> findByProduct_ProductId(Integer productId);
}
