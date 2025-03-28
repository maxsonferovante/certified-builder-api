package com.maal.certifiedbuilderapi.infrastructure.aws.sqs;

import com.maal.certifiedbuilderapi.domain.entity.CertificateEntity;
import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import com.maal.certifiedbuilderapi.infrastructure.aws.sqs.payload.OrderEvent;
import com.maal.certifiedbuilderapi.infrastructure.repository.CertificateRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);
    private final OrderRepository orderRepository;
    private final CertificateRepository certificateRepository;

    @SqsListener(value = "${spring.cloud.aws.queue.name.notification.generation}")
    public void receiveOrderEvent(OrderEvent orderEvent) {
        processOrderEvent(orderEvent);
    }

    private void processOrderEvent(OrderEvent orderEvent) {
        logger.info("Received and Processing order event for orderId: {}", orderEvent.getOrderId());
        Optional<OrderEntity> orderEntity = orderRepository.findByOrderId(orderEvent.getOrderId());
        if (orderEntity.isPresent()) {
            logger.info("Found order entity for orderId: {}", orderEntity.get().getOrderId());
            CertificateEntity certificateEntity = certificateRepository.findByOrder(orderEntity.get())
                    .map(existingCertificate -> {
                        if (!existingCertificate.getSuccess() && orderEvent.getSuccess()) {
                            logger.info("Updating certificate for orderId: {} with success=true", orderEntity.get().getOrderId());
                            existingCertificate.setCertificateKey(orderEvent.getCertificateKey());
                            existingCertificate.setSuccess(orderEvent.getSuccess());
                            return certificateRepository.save(existingCertificate);
                        }
                        return existingCertificate;
                    })
                    .orElseGet(() -> {
                        CertificateEntity certificate = new CertificateEntity();
                        certificate.setCertificateKey(orderEvent.getCertificateKey());
                        certificate.setSuccess(orderEvent.getSuccess());
                        certificate.setOrder(orderEntity.get());
                        return certificateRepository.save(certificate);
                    });

            logger.info("Certificate generated for orderId: {} - {}", orderEntity.get().getOrderId(), certificateEntity.getSuccess());
        } else {
            logger.info("No order found for orderId: {}", orderEvent.getOrderId());
        }
    }
}
