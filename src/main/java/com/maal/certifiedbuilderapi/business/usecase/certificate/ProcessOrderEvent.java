package com.maal.certifiedbuilderapi.business.usecase.certificate;

import com.maal.certifiedbuilderapi.domain.event.OrderEvent;
import com.maal.certifiedbuilderapi.domain.entity.CertificateEntity;
import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import com.maal.certifiedbuilderapi.infrastructure.aws.s3.S3ClientCustomer;
import com.maal.certifiedbuilderapi.infrastructure.repository.CertificateRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
        certificateRepository.findByOrder(order)
                .map(existingCertificate -> updateExistingCertificate(existingCertificate, event))
                .orElseGet(() -> createNewCertificate(order, event));
    }

    /**
     * Updates an existing certificate based on the event.
     *
     * @param certificate The existing certificate
     * @param event The order event
     * @return The updated certificate
     */
    private CertificateEntity updateExistingCertificate(CertificateEntity certificate, OrderEvent event) {
        Boolean eventSuccess = event.getSuccess();
        if (eventSuccess != null && eventSuccess) {
            s3ClientCustomer.deleteCertificate(certificate.getCertificateKey());
            
            certificate.setCertificateKey(event.getCertificateKey());
            certificate.setCertificateUrl(s3ClientCustomer.getUrl(event.getCertificateKey()));
            certificate.setSuccess(true);
            certificate.setGeneretedDate(LocalDateTime.now());
        }
        return certificateRepository.save(certificate);
    }

    /**
     * Creates a new certificate based on the event.
     *
     * @param order The order entity
     * @param event The order event
     * @return The new certificate
     */
    private CertificateEntity createNewCertificate(OrderEntity order, OrderEvent event) {
        CertificateEntity certificate = new CertificateEntity();
        certificate.setOrder(order);
        certificate.setSuccess(event.getSuccess() != null ? event.getSuccess() : false);
        
        if (Boolean.TRUE.equals(event.getSuccess())) {
            certificate.setCertificateKey(event.getCertificateKey());
            certificate.setCertificateUrl(s3ClientCustomer.getUrl(event.getCertificateKey()));
            certificate.setGeneretedDate(LocalDateTime.now());
        }
        
        return certificateRepository.save(certificate);
    }
} 