package com.maal.certifiedbuilderapi.business.usecase.certificate;

import com.maal.certifiedbuilderapi.business.dto.RecoverCertificatesResponse;
import com.maal.certifiedbuilderapi.business.mapper.CertificanteMapper;
import com.maal.certifiedbuilderapi.domain.event.OrderEvent;
import com.maal.certifiedbuilderapi.domain.entity.CertificateEntity;
import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import com.maal.certifiedbuilderapi.infrastructure.aws.s3.S3ClientCustomer;
import com.maal.certifiedbuilderapi.infrastructure.client.TechFloripa;
import com.maal.certifiedbuilderapi.infrastructure.repository.CertificateRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Use case responsible for processing order events and managing certificates.
 */
@Service
@RequiredArgsConstructor
public class ProcessOrderEvent {

    private static final Logger logger = LoggerFactory.getLogger(ProcessOrderEvent.class);

    private final OrderRepository orderRepository;
    private final CertificateRepository certificateRepository;
    private final S3ClientCustomer s3ClientCustomer;
    private final CertificanteMapper certificanteMapper;
    private final TechFloripa techFloripa;

    /**
     * Processes an order event and updates or creates the associated certificate.
     *
     * @param orderEvent The order event to process
     */
    @Transactional
    public void execute(OrderEvent orderEvent) {
        OrderEntity order =  orderRepository.findByOrderId(orderEvent.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderEvent.getOrderId()));

        processCertificate(order, orderEvent);
    }

    /**
     * Processes the certificate for a given order and event.
     *
     * @param order The order entity
     * @param event The order event
     */
    private void processCertificate(OrderEntity order, OrderEvent event) {
        Optional<CertificateEntity> existingCertificate = certificateRepository.findByOrder(order);
        existingCertificate.ifPresentOrElse(
                certificateEntity -> updateExistingCertificate(certificateEntity, event),
                () -> createNewCertificate(order, event));
    }

    /**
     * Updates an existing certificate based on the event.
     *
     * @param certificate The existing certificate
     * @param event       The order event
     */
    private void updateExistingCertificate(CertificateEntity certificate, OrderEvent event) {
        Boolean eventSuccess = event.getSuccess();
        if (eventSuccess != null && eventSuccess) {
            s3ClientCustomer.deleteCertificate(certificate.getCertificateKey());
            
            certificate.setCertificateKey(event.getCertificateKey());
            certificate.setCertificateUrl(s3ClientCustomer.getUrl(event.getCertificateKey()));
            certificate.setSuccess(true);
            certificate.setGeneretedDate(LocalDateTime.now());
        }
        certificateRepository.save(certificate);
        notifiesCertificateGeneration(certificate);
    }

    /**
     * Creates a new certificate based on the event.
     *
     * @param order The order entity
     * @param event The order event
     */
    private void createNewCertificate(OrderEntity order, OrderEvent event) {
        CertificateEntity certificate = new CertificateEntity();
        certificate.setOrder(order);
        certificate.setSuccess(event.getSuccess() != null && event.getSuccess());
        
        if (Boolean.TRUE.equals(event.getSuccess())) {
            certificate.setCertificateKey(event.getCertificateKey());
            certificate.setCertificateUrl(s3ClientCustomer.getUrl(event.getCertificateKey()));
            certificate.setGeneretedDate(LocalDateTime.now());
        }

        certificateRepository.save(certificate);
        notifiesCertificateGeneration(certificate);
    }

    private void notifiesCertificateGeneration(CertificateEntity certificate) {
        RecoverCertificatesResponse recoverCertificatesResponse = certificanteMapper.certificateEntityToResponse(
                certificate
        );
        techFloripa.notifiesCertificateGeneration(recoverCertificatesResponse);

    }
} 