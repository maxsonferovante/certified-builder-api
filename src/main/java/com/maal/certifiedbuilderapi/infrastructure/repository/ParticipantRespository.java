package com.maal.certifiedbuilderapi.infrastructure.repository;

import com.maal.certifiedbuilderapi.domain.entity.ParticipantEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository para ParticipantEntity usando AWS SDK v2.x Enhanced DynamoDB Client
 * Implementa operações de persistência usando Enhanced DynamoDB diretamente
 */
@Repository
public class ParticipantRespository {

    private final DynamoDbTable<ParticipantEntity> participantTable;

    @Autowired
    public ParticipantRespository(DynamoDbTable<ParticipantEntity> participantTable) {
        this.participantTable = participantTable;
    }

    /**
     * Salva ou atualiza um participante
     */
    public ParticipantEntity save(ParticipantEntity participant) {
        participantTable.putItem(participant);
        return participant;
    }

    /**
     * Busca participante por ID
     */
    public Optional<ParticipantEntity> findById(String id) {
        ParticipantEntity participant = participantTable.getItem(Key.builder().partitionValue(id).build());
        return Optional.ofNullable(participant);
    }

    /**
     * Busca participante por email
     * Usa scan com filtro (para busca por índice secundário seria necessário configurar GSI)
     */
    public Optional<ParticipantEntity> findByEmail(String email) {
        return participantTable.scan(ScanEnhancedRequest.builder()
                .filterExpression(software.amazon.awssdk.enhanced.dynamodb.Expression.builder()
                    .expression("email = :email")
                    .putExpressionValue(":email", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder()
                        .s(email)
                        .build())
                    .build())
                .build())
            .items()
            .stream()
            .findFirst();
    }

    /**
     * Lista todos os participantes
     */
    public List<ParticipantEntity> findAll() {
        return participantTable.scan().items().stream().collect(Collectors.toList());
    }

    /**
     * Busca participantes por CPF
     */
    public Optional<ParticipantEntity> findByCpf(String cpf) {
        return participantTable.scan(ScanEnhancedRequest.builder()
                .filterExpression(software.amazon.awssdk.enhanced.dynamodb.Expression.builder()
                    .expression("cpf = :cpf")
                    .putExpressionValue(":cpf", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder()
                        .s(cpf)
                        .build())
                    .build())
                .build())
            .items()
            .stream()
            .findFirst();
    }

    /**
     * Busca participantes por cidade
     */
    public List<ParticipantEntity> findByCity(String city) {
        return participantTable.scan(ScanEnhancedRequest.builder()
                .filterExpression(software.amazon.awssdk.enhanced.dynamodb.Expression.builder()
                    .expression("city = :city")
                    .putExpressionValue(":city", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder()
                        .s(city)
                        .build())
                    .build())
                .build())
            .items()
            .stream()
            .collect(Collectors.toList());
    }

    /**
     * Deleta participante por ID
     */
    public void deleteById(String id) {
        participantTable.deleteItem(Key.builder().partitionValue(id).build());
    }

    /**
     * Verifica se existe participante com o ID
     */
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    /**
     * Conta total de participantes
     */
    public long count() {
        return participantTable.scan().items().stream().count();
    }
}
