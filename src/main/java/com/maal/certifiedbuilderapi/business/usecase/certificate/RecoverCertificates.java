package com.maal.certifiedbuilderapi.business.usecase.certificate;

import com.maal.certifiedbuilderapi.business.dto.RecoverCertificatesResponse;
import com.maal.certifiedbuilderapi.business.mapper.CertificanteMapper;
import com.maal.certifiedbuilderapi.domain.entity.CertificateEntity;
import com.maal.certifiedbuilderapi.infrastructure.aws.s3.S3ClientCustomer;
import com.maal.certifiedbuilderapi.infrastructure.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Use case responsible for recovering and updating certificate URLs.
 * Adaptado para trabalhar com dados desnormalizados do DynamoDB
 * This includes:
 * - Retrieving certificates for a specific product
 * - Updating certificate URLs if they are older than 7 days
 * - Mapping certificates to response DTOs
 */
@Service
@RequiredArgsConstructor
public class RecoverCertificates {

    private static final int CERTIFICATE_EXPIRY_DAYS = 7;

    private final CertificateRepository certificateRepository;
    private final CertificanteMapper certificanteMapper;
    private final S3ClientCustomer s3ClientCustomer;

    /**
     * Recovers and updates certificates for a specific product.
     *
     * @param productId The ID of the product whose certificates should be recovered
     * @return List of RecoverCertificatesResponse containing updated certificate information
     */
    @Transactional(readOnly = true)
    public List<RecoverCertificatesResponse> execute(Integer productId) {
        List<CertificateEntity> certificates = findCertificates(productId);
        updateExpiredCertificates(certificates);
        return mapToResponse(certificates);
    }

    /**
     * Finds all certificates for a specific product.
     * Agora usa dados desnormalizados: busca diretamente por productId
     *
     * @param productId The ID of the product
     * @return List of certificates
     */
    private List<CertificateEntity> findCertificates(Integer productId) {
        // Método atualizado para DynamoDB - busca direta por productId desnormalizado
        return certificateRepository.findByProductId(productId);
    }

    /**
     * Updates certificates that are older than the expiry period.
     *
     * @param certificates List of certificates to check and update
     */
    private void updateExpiredCertificates(List<CertificateEntity> certificates) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.minusDays(CERTIFICATE_EXPIRY_DAYS);

        certificates.stream()
                .filter(this::isValidCertificate)
                .filter(certificate -> isExpired(certificate, expiryDate))
                .forEach(certificate -> updateCertificate(certificate, now));
    }

    /**
     * Checks if a certificate is valid (successfully generated).
     *
     * @param certificate The certificate to check
     * @return true if the certificate is valid
     */
    private boolean isValidCertificate(CertificateEntity certificate) {
        return certificate.getSuccess();
    }

    /**
     * Checks if a certificate has expired based on the given expiry date.
     * Agora usa o método utilitário para converter String para LocalDateTime
     *
     * @param certificate The certificate to check
     * @param expiryDate The date after which certificates are considered expired
     * @return true if the certificate has expired
     */
    private boolean isExpired(CertificateEntity certificate, LocalDateTime expiryDate) {
        // Usa método utilitário para converter String para LocalDateTime
        LocalDateTime generatedDateTime = certificate.getGeneratedDateAsLocalDateTime();
        if (generatedDateTime == null) {
            return true; // Se não tem data de geração, considera expirado
        }
        return generatedDateTime.isBefore(expiryDate) || generatedDateTime.equals(expiryDate);
    }

    /**
     * Updates a certificate's URL and generation date.
     * Agora usa método utilitário para converter LocalDateTime para String
     *
     * @param certificate The certificate to update
     * @param now The current timestamp
     */
    private void updateCertificate(CertificateEntity certificate, LocalDateTime now) {
        String newUrl = s3ClientCustomer.getUrl(certificate.getCertificateKey());
        certificate.setCertificateUrl(newUrl);
        // Usa método utilitário para converter LocalDateTime para String compatível com DynamoDB
        certificate.setGeneratedDateFromLocalDateTime(now);
        certificateRepository.save(certificate);
    }

    /**
     * Maps certificates to response DTOs.
     *
     * @param certificates List of certificates to map
     * @return List of RecoverCertificatesResponse
     */
    private List<RecoverCertificatesResponse> mapToResponse(List<CertificateEntity> certificates) {
        return certificanteMapper.certificateEntityListToResponseList(certificates);
    }
}
