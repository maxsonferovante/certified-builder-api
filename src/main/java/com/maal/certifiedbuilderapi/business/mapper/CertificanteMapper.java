package com.maal.certifiedbuilderapi.business.mapper;

import com.maal.certifiedbuilderapi.business.dto.RecoverCertificatesResponse;
import com.maal.certifiedbuilderapi.domain.entity.CertificateEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper para conversão de CertificateEntity para RecoverCertificatesResponse
 * Adaptado para trabalhar com dados desnormalizados do DynamoDB
 */
@Mapper(componentModel = "spring")
public interface CertificanteMapper {
    
    /**
     * Converte lista de entidades Certificate para lista de responses
     * @param certificateEntities Lista de entidades
     * @return Lista de responses
     */
    default List<RecoverCertificatesResponse> certificateEntityListToResponseList(List<CertificateEntity> certificateEntities) {
        return certificateEntities.stream().map(this::getRecoverCertificatesResponse).toList();
    }

    /**
     * Converte entidade Certificate para response
     * @param certificateEntity Entidade do certificado
     * @return Response com dados do certificado
     */
    default RecoverCertificatesResponse certificateEntityToResponse(CertificateEntity certificateEntity) {
        return getRecoverCertificatesResponse(certificateEntity);
    }

    /**
     * Método privado que faz a conversão usando campos desnormalizados
     * Não precisa mais navegar por objetos aninhados (Order -> Product)
     * Agora converte String para LocalDateTime conforme necessário
     * @param certificateEntity Entidade com dados desnormalizados
     * @return Response construído
     */
    private RecoverCertificatesResponse getRecoverCertificatesResponse(CertificateEntity certificateEntity) {
        return RecoverCertificatesResponse
                .builder()
                // === DADOS DO CERTIFICADO ===
                .certificateId(certificateEntity.getId())
                .certificateUrl(certificateEntity.getCertificateUrl())
                // Converte String para LocalDateTime usando método utilitário
                .generetedDate(certificateEntity.getGeneratedDateAsLocalDateTime())
                .success(certificateEntity.getSuccess())
                
                // === DADOS DESNORMALIZADOS DO PRODUTO ===
                .productId(certificateEntity.getProductId())         // Direto do campo desnormalizado
                .productName(certificateEntity.getProductName())     // Direto do campo desnormalizado
                
                // === DADOS DESNORMALIZADOS DO PEDIDO ===
                .orderId(certificateEntity.getOrderId())             // Direto do campo desnormalizado
                // Converte String para LocalDateTime usando método utilitário
                .orderDate(certificateEntity.getOrderDateAsLocalDateTime())
                
                .build();
    }
}
