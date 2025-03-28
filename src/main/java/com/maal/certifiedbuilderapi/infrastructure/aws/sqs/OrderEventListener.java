package com.maal.certifiedbuilderapi.infrastructure.aws.sqs;


import com.maal.certifiedbuilderapi.domain.entity.CertificateEntity;
import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import com.maal.certifiedbuilderapi.infrastructure.aws.sqs.payload.OrderEvent;
import com.maal.certifiedbuilderapi.infrastructure.repository.CertificateRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRespository;
import io.awspring.cloud.sqs.annotation.SqsListener;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Retention;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);
    private final OrderRespository orderRespository;
    private final CertificateRepository certificateRepository;

    @SqsListener(value = "${spring.cloud.aws.queue.name.notification.generation}")
    public void ReceiverOrderEvent(OrderEvent orderEvent) {
        processOrderEvent(orderEvent);
    }

    private void processOrderEvent(OrderEvent orderEvent) {
        logger.info("Received and Processing order event for orderId: {}", orderEvent.getOrderId());
        Optional<OrderEntity> orderEntity = orderRespository.findByOrderId(orderEvent.getOrderId());
        if (orderEntity.isPresent()) {
            logger.info("Found order entity for orderId: {}", orderEntity.get().getOrderId());
            CertificateEntity certificateEntity = certificateRepository.findByOrder(orderEntity.get())
                    .orElseGet(() ->{
                        CertificateEntity certificate = new CertificateEntity();
                        certificate.setCertificateKey(orderEvent.getCertificateKey());
                        certificate.setSuccess(orderEvent.getSuccess());
                        certificate.setOrder(orderEntity.get());
                        return certificateRepository.save(certificate);
                    });

            logger.info("Found certificate for orderId: {}", orderEntity.get().getOrderId());
            logger.info("Certificate generated for orderId: {} - {}", orderEntity.get().getOrderId(), certificateEntity.getSuccess());
        } else {
            logger.info("No order found for orderId: {}", orderEvent.getOrderId());
        }

    }
}
