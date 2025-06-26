package com.maal.certifiedbuilderapi.domain.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Entidade Participant para DynamoDB
 * Migrada para AWS SDK v2.x Enhanced DynamoDB
 */
@Data
@NoArgsConstructor
@DynamoDbBean
public class ParticipantEntity {

    // Campos sem anotações - as anotações vão nos getters
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String cpf;
    private String city;
    
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
     * Getter para Email - Índice secundário para consultas por email
     */
    @DynamoDbSecondaryPartitionKey(indexNames = "EmailIndex")
    public String getEmail() {
        return email;
    }
}
