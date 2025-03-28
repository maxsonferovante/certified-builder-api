package com.maal.certifiedbuilderapi.business.usecases;


import com.maal.certifiedbuilderapi.business.dto.RecoverCertificatesResponse;
import com.maal.certifiedbuilderapi.business.mapper.CertificanteMapper;
import com.maal.certifiedbuilderapi.domain.entity.CertificateEntity;
import com.maal.certifiedbuilderapi.infrastructure.aws.s3.S3ClientCustomer;
import com.maal.certifiedbuilderapi.infrastructure.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecoverCertificates {

    private final CertificateRepository certificateRepository;
    private final CertificanteMapper certificanteMapper;
    private final S3ClientCustomer s3ClientCustomer;

    public List<RecoverCertificatesResponse> execute(Integer productId) {
        List<CertificateEntity> certificateEntityList = certificateRepository.findByOrder_Product_ProductId(productId);
        List<RecoverCertificatesResponse> recoverCertificatesResponseList = certificanteMapper.certificateEntityListToResponseList(certificateEntityList);

        return recoverCertificatesResponseList.stream()
                .peek(
            certificanteResponse -> certificanteResponse.setCertificateUrl(
                    s3ClientCustomer.getUrl(certificanteResponse.getCertificateUrl())
            )
        ).toList();
    }
}
