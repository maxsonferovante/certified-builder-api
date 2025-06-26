package com.maal.certifiedbuilderapi.infrastructure.repository;

import com.maal.certifiedbuilderapi.domain.entity.CertificateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository para CertificateEntity usando AWS SDK v2.x Enhanced DynamoDB Client
 * Implementa operações de persistência usando Enhanced DynamoDB diretamente
 */
@Repository
public class CertificateRepository {

    private final DynamoDbTable<CertificateEntity> certificateTable;

    @Autowired
    public CertificateRepository(DynamoDbTable<CertificateEntity> certificateTable) {
        this.certificateTable = certificateTable;
    }

    /**
     * Salva ou atualiza um certificado
     */
    public CertificateEntity save(CertificateEntity certificate) {
        certificateTable.putItem(certificate);
        return certificate;
    }

    /**
     * Busca certificado por ID
     */
    public Optional<CertificateEntity> findById(String id) {
        CertificateEntity certificate = certificateTable.getItem(Key.builder().partitionValue(id).build());
        return Optional.ofNullable(certificate);
    }

    /**
     * Busca certificado por orderId
     * Usa scan com filtro (para busca por índice secundário seria necessário configurar GSI)
     */
    public Optional<CertificateEntity> findByOrderId(Integer orderId) {
        return certificateTable.scan(ScanEnhancedRequest.builder()
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
     * Lista todos os certificados
     * Usa scan completo - cuidado com performance em tabelas grandes
     */
    public List<CertificateEntity> findAll() {
        return certificateTable.scan().items().stream().collect(Collectors.toList());
    }

    /**
     * Busca certificados por productId
     * Usa scan com filtro
     */
    public List<CertificateEntity> findByProductId(Integer productId) {
        return certificateTable.scan(ScanEnhancedRequest.builder()
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
     * Conta certificados por sucesso
     */
    public long countBySuccess(Boolean success) {
        return certificateTable.scan(ScanEnhancedRequest.builder()
                .filterExpression(software.amazon.awssdk.enhanced.dynamodb.Expression.builder()
                    .expression("success = :success")
                    .putExpressionValue(":success", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder()
                        .bool(success)
                        .build())
                    .build())
                .build())
            .items()
            .stream()
            .count();
    }

    /**
     * Deleta certificado por ID
     */
    public void deleteById(String id) {
        certificateTable.deleteItem(Key.builder().partitionValue(id).build());
    }

    /**
     * Remove certificados por productId
     * Localiza todos os certificados com o productId e remove cada um
     */
    public void deleteByProductId(Integer productId) {
        List<CertificateEntity> certificates = findByProductId(productId);
        certificates.forEach(cert -> certificateTable.deleteItem(Key.builder().partitionValue(cert.getId()).build()));
    }

    /**
     * Verifica se existe certificado com o ID
     */
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    /**
     * Conta total de certificados
     */
    public long count() {
        return certificateTable.scan().items().stream().count();
    }
}
