package com.maal.certifiedbuilderapi.business.mapper;

import com.maal.certifiedbuilderapi.business.dto.RecoverCertificatesResponse;
import com.maal.certifiedbuilderapi.domain.entity.CertificateEntity;
import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")


public interface CertificanteMapper {
    default List<RecoverCertificatesResponse>  certificateEntityListToResponseList(List<CertificateEntity> certificateEntities) {
        return certificateEntities.stream().map(this::getRecoverCertificatesResponse).toList();
    }

    default RecoverCertificatesResponse certificateEntityToResponse(CertificateEntity certificateEntity) {
        return getRecoverCertificatesResponse(certificateEntity);
    }

    private RecoverCertificatesResponse getRecoverCertificatesResponse(CertificateEntity certificateEntity) {
        OrderEntity orderEntity = certificateEntity.getOrder();
        return RecoverCertificatesResponse
                .builder()
                .certificateId(certificateEntity.getId())
                .certificateUrl(certificateEntity.getCertificateUrl())
                .generetedDate(certificateEntity.getGeneretedDate())
                .success(certificateEntity.getSuccess())
                .productId(Objects.requireNonNull(orderEntity).getProduct().getProductId())
                .productName(Objects.requireNonNull(orderEntity).getProduct().getProductName())
                .orderId(Objects.requireNonNull(orderEntity).getOrderId())
                .orderDate(Objects.requireNonNull(orderEntity).getOrderDate())
                .build();
    }
}
