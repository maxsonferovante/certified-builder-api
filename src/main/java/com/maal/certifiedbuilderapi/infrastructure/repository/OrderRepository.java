package com.maal.certifiedbuilderapi.infrastructure.repository;

import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository para OrderEntity usando AWS SDK v2.x Enhanced DynamoDB Client
 * Implementa operações de persistência usando Enhanced DynamoDB diretamente
 */
@Repository
public class OrderRepository {

    private final DynamoDbTable<OrderEntity> orderTable;

    @Autowired
    public OrderRepository(DynamoDbTable<OrderEntity> orderTable) {
        this.orderTable = orderTable;
    }

    /**
     * Salva ou atualiza um pedido
     */
    public OrderEntity save(OrderEntity order) {
        orderTable.putItem(order);
        return order;
    }

    /**
     * Busca pedido por ID
     */
    public Optional<OrderEntity> findById(String id) {
        OrderEntity order = orderTable.getItem(Key.builder().partitionValue(id).build());
        return Optional.ofNullable(order);
    }

    /**
     * Busca pedido por orderId único
     * Usa scan com filtro para localizar por orderId
     */
    public Optional<OrderEntity> findByOrderId(Integer orderId) {
        return orderTable.scan(ScanEnhancedRequest.builder()
                .filterExpression(software.amazon.awssdk.enhanced.dynamodb.Expression.builder()
                    .expression("orderId = :orderId")
                    .putExpressionValue(":orderId", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder()
                        .n(String.valueOf(orderId))
                        .build())
                    .build())
                .build())
            .items()
            .stream()
            .findFirst();
    }

    /**
     * Lista pedidos por productId
     * Usa scan com filtro
     */
    public List<OrderEntity> findByProductId(Integer productId) {
        return orderTable.scan(ScanEnhancedRequest.builder()
                .filterExpression(software.amazon.awssdk.enhanced.dynamodb.Expression.builder()
                    .expression("productId = :productId")
                    .putExpressionValue(":productId", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder()
                        .n(String.valueOf(productId))
                        .build())
                    .build())
                .build())
            .items()
            .stream()
            .collect(Collectors.toList());
    }

    /**
     * Busca pedidos por email do participante
     * Usa scan com filtro
     */
    public List<OrderEntity> findByParticipantEmail(String participantEmail) {
        return orderTable.scan(ScanEnhancedRequest.builder()
                .filterExpression(software.amazon.awssdk.enhanced.dynamodb.Expression.builder()
                    .expression("participantEmail = :email")
                    .putExpressionValue(":email", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder()
                        .s(participantEmail)
                        .build())
                    .build())
                .build())
            .items()
            .stream()
            .collect(Collectors.toList());
    }

    /**
     * Remove pedidos por productId
     * Localiza todos os pedidos com o productId e remove cada um
     */
    public void deleteByProductId(Integer productId) {
        List<OrderEntity> orders = findByProductId(productId);
        orders.forEach(order -> orderTable.deleteItem(Key.builder().partitionValue(order.getId()).build()));
    }

    /**
     * Lista todos os pedidos
     */
    public List<OrderEntity> findAll() {
        return orderTable.scan().items().stream().collect(Collectors.toList());
    }

    /**
     * Deleta pedido por ID
     */
    public void deleteById(String id) {
        orderTable.deleteItem(Key.builder().partitionValue(id).build());
    }

    /**
     * Verifica se existe pedido com o ID
     */
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }
}
