package com.maal.certifiedbuilderapi.infrastructure.repository;

import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ParticipantRespository extends MongoRepository<ProductEntity, String> {
}
