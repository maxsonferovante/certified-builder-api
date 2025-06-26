package com.maal.certifiedbuilderapi.domain.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade Order para DynamoDB
 * Dados desnormalizados para melhor performance no DynamoDB
 * Migrada para AWS SDK v2.x
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class OrderEntity {

    // Campos sem anotações - as anotações vão nos getters
    private String id;
    private Integer orderId;
    private String orderDate;
    private Integer productId;
    private String productName;
    private String certificateDetails;
    private String certificateLogo;
    private String certificateBackground;
    private String checkinLatitude;
    private String checkinLongitude;
    private String timeCheckin;
    private String participantEmail;
    private String participantFirstName;
    private String participantLastName;
    private String participantCpf;
    private String participantPhone;
    private String participantCity;
    
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
     * Getter para OrderId - Índice secundário para consultas
     */
    @DynamoDbSecondaryPartitionKey(indexNames = "OrderIdIndex")
    public Integer getOrderId() {
        return orderId;
    }
    
    // === MÉTODOS UTILITÁRIOS PARA CONVERSÃO DE DATA ===
    
    /**
     * Converte o orderDate (String) para LocalDateTime
     * Facilita o uso em lógicas que precisam trabalhar com LocalDateTime
     */
    public LocalDateTime getOrderDateAsLocalDateTime() {
        return orderDate != null ? LocalDateTime.parse(orderDate) : null;
    }
    
    /**
     * Define o orderDate a partir de um LocalDateTime
     * Converte automaticamente para String para compatibilidade com DynamoDB
     */
    public void setOrderDateFromLocalDateTime(LocalDateTime dateTime) {
        this.orderDate = dateTime != null ? dateTime.toString() : null;
    }
}
