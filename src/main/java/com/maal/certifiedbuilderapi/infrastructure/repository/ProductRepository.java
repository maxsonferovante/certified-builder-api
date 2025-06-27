package com.maal.certifiedbuilderapi.infrastructure.repository;

import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository para ProductEntity usando AWS SDK v2.x Enhanced DynamoDB Client
 * Implementa operações de persistência usando Enhanced DynamoDB diretamente
 */
@Repository
public class ProductRepository {

    private final DynamoDbTable<ProductEntity> productTable;

    @Autowired
    public ProductRepository(DynamoDbTable<ProductEntity> productTable) {
        this.productTable = productTable;
    }

    /**
     * Salva ou atualiza um produto
     */
    public ProductEntity save(ProductEntity product) {
        productTable.putItem(product);
        return product;
    }

    /**
     * Busca produto por ID
     */
    public Optional<ProductEntity> findById(String id) {
        ProductEntity product = productTable.getItem(Key.builder().partitionValue(id).build());
        return Optional.ofNullable(product);
    }

    /**
     * Busca produto por productId
     * Usa scan com filtro (para busca por índice secundário seria necessário configurar GSI)
     */
    public Optional<ProductEntity> findByProductId(Integer productId) {
        return productTable.scan(ScanEnhancedRequest.builder()
                .filterExpression(software.amazon.awssdk.enhanced.dynamodb.Expression.builder()
                    .expression("productId = :productId")
                    .putExpressionValue(":productId", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder()
                        .n(String.valueOf(productId))
                        .build())
                    .build())
                .build())
            .items()
            .stream()
            .findFirst();
    }

    /**
     * Remove produto por productId
     * Localiza o produto pelo productId e remove
     */
    public void deleteByProductId(Integer productId) {
        Optional<ProductEntity> product = findByProductId(productId);
        product.ifPresent(p -> productTable.deleteItem(Key.builder().partitionValue(p.getId()).build()));
    }

    /**
     * Lista todos os produtos
     */
    public List<ProductEntity> findAll() {
        return productTable.scan().items().stream().collect(Collectors.toList());
    }

    /**
     * Busca produtos por nome
     */
    public List<ProductEntity> findByProductName(String productName) {
        return productTable.scan(ScanEnhancedRequest.builder()
                .filterExpression(software.amazon.awssdk.enhanced.dynamodb.Expression.builder()
                    .expression("productName = :productName")
                    .putExpressionValue(":productName", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder()
                        .s(productName)
                        .build())
                    .build())
                .build())
            .items()
            .stream()
            .collect(Collectors.toList());
    }

    /**
     * Busca produtos que contêm determinado texto no nome
     */
    public List<ProductEntity> findByProductNameContaining(String nameFragment) {
        return productTable.scan(ScanEnhancedRequest.builder()
                .filterExpression(software.amazon.awssdk.enhanced.dynamodb.Expression.builder()
                    .expression("contains(productName, :nameFragment)")
                    .putExpressionValue(":nameFragment", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder()
                        .s(nameFragment)
                        .build())
                    .build())
                .build())
            .items()
            .stream()
            .collect(Collectors.toList());
    }

    /**
     * Deleta produto por ID
     */
    public void deleteById(String id) {
        productTable.deleteItem(Key.builder().partitionValue(id).build());
    }

    /**
     * Verifica se existe produto com o ID
     */
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    /**
     * Verifica se existe produto com o productId
     */
    public boolean existsByProductId(Integer productId) {
        return findByProductId(productId).isPresent();
    }

    /**
     * Conta total de produtos
     */
    public long count() {
        return productTable.scan().items().stream().count();
    }
}
