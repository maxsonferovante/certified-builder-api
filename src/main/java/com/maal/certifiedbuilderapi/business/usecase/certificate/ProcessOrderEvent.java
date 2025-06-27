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
 * Adaptado para trabalhar com dados desnormalizados do DynamoDB
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
        OrderEntity order = orderRepository.findByOrderId(orderEvent.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderEvent.getOrderId()));

        logger.info("Processing order: {}", orderEvent.getOrderId());
        processCertificate(order, orderEvent);
    }

    /**
     * Processes the certificate for a given order and event.
     * Agora usa orderId para buscar o certificado (dados desnormalizados)
     *
     * @param order The order entity
     * @param event The order event
     */
    private void processCertificate(OrderEntity order, OrderEvent event) {
        // Busca certificado usando orderId (dados desnormalizados)
        Optional<CertificateEntity> existingCertificate = certificateRepository.findByOrderId(order.getOrderId());
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
            certificate.setGeneratedDateFromLocalDateTime(LocalDateTime.now());
        }
        logger.info("Update register certificate by order {} with status success {}", event.getOrderId(), event.getSuccess());
        certificateRepository.save(certificate);
        notifiesCertificateGeneration(certificate);
    }

    /**
     * Creates a new certificate based on the event.
     * Agora preenche campos desnormalizados em vez de setar o objeto Order
     *
     * @param order The order entity
     * @param event The order event
     */
    private void createNewCertificate(OrderEntity order, OrderEvent event) {
        CertificateEntity certificate = new CertificateEntity();
        
        // === DADOS DESNORMALIZADOS DO PEDIDO ===
        certificate.setOrderId(order.getOrderId());
        certificate.setOrderDate(order.getOrderDate());
        
        // === DADOS DESNORMALIZADOS DO PRODUTO ===
        certificate.setProductId(order.getProductId());
        certificate.setProductName(order.getProductName());
        certificate.setCertificateDetails(order.getCertificateDetails());
        certificate.setCertificateLogo(order.getCertificateLogo());
        certificate.setCertificateBackground(order.getCertificateBackground());
        
        // === DADOS DESNORMALIZADOS DO PARTICIPANTE ===
        certificate.setParticipantEmail(order.getParticipantEmail());
        certificate.setParticipantFirstName(order.getParticipantFirstName());
        certificate.setParticipantLastName(order.getParticipantLastName());
        certificate.setParticipantCpf(order.getParticipantCpf());
        certificate.setParticipantPhone(order.getParticipantPhone());
        certificate.setParticipantCity(order.getParticipantCity());
        
        // === DADOS DO CERTIFICADO ===
        certificate.setSuccess(event.getSuccess() != null && event.getSuccess());
        
        if (Boolean.TRUE.equals(event.getSuccess())) {
            certificate.setCertificateKey(event.getCertificateKey());
            certificate.setCertificateUrl(s3ClientCustomer.getUrl(event.getCertificateKey()));
            certificate.setGeneratedDateFromLocalDateTime(LocalDateTime.now());
        }
        
        logger.info("Create register certificate by order {} with status success {}", event.getOrderId(), event.getSuccess());
        certificateRepository.save(certificate);
        notifiesCertificateGeneration(certificate);
    }

    private void notifiesCertificateGeneration(CertificateEntity certificate) {
        try {
            RecoverCertificatesResponse recoverCertificatesResponse = certificanteMapper.certificateEntityToResponse(
                    certificate
            );
            techFloripa.notifiesCertificateGeneration(recoverCertificatesResponse);
            logger.info("Notified certificate generation for order {}", certificate.getOrderId());
        } catch (Exception e) {
            logger.error("Error notifying certificate generation: {}", e.getMessage());
        }
    }
} 