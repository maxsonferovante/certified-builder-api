package com.maal.certifiedbuilderapi.infrastructure.repository;

import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderRespository  extends MongoRepository<OrderEntity, String> {
}
