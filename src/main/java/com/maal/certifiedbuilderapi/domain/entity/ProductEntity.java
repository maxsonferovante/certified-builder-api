package com.maal.certifiedbuilderapi.domain.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Entidade Product para DynamoDB
 * Migrada para AWS SDK v2.x Enhanced DynamoDB
 */
@Data
@NoArgsConstructor
@DynamoDbBean
public class ProductEntity {

    // Campos sem anotações - as anotações vão nos getters
    private String id;
    private Integer productId;
    private String productName;
    private String certificateDetails;
    private String certificateLogo;
    private String certificateBackground;
    private String checkinLatitude;
    private String checkinLongitude;
    private String timeCheckin;
    
    // === GETTERS COM ANOTAÇÕES DYNAMODB ===
    
    /**
     * Getter para ID - Chave de partição
     * Gera UUID automaticamente se não estiver definido
     */
    @DynamoDbPartitionKey
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }
    
    /**
     * Getter para ProductId - Índice secundário para consultas por productId
     */
    @DynamoDbSecondaryPartitionKey(indexNames = "ProductIdIndex")
    public Integer getProductId() {
        return productId;
    }
}
