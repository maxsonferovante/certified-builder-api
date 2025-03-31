package com.maal.certifiedbuilderapi.business.usecase.certificate;

import com.maal.certifiedbuilderapi.business.dto.BuildOrdersRequest;
import com.maal.certifiedbuilderapi.business.dto.BuildOrdersResponse;
import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import com.maal.certifiedbuilderapi.domain.entity.ParticipantEntity;
import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import com.maal.certifiedbuilderapi.infrastructure.aws.sqs.OrderEventPublisher;
import com.maal.certifiedbuilderapi.infrastructure.client.TechFloripa;
import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;
import com.maal.certifiedbuilderapi.infrastructure.repository.CertificateRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ParticipantRespository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CertificateConstructionOrder {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TechFloripa techFloripa;
    private final OrderRepository orderRepository;
    private final ParticipantRespository participantRespository;
    private final ProductRepository productRepository;
    private final OrderEventPublisher orderEventPublisher;

    /**
     * Processes orders from TechFloripa and creates certificates for new orders.
     *
     * @param request The build orders request containing the product ID
     * @return BuildOrdersResponse with processing results
     */
    public BuildOrdersResponse execute(BuildOrdersRequest request) {
        List<TechOrdersResponse> orders = techFloripa.getOrders(request.getProductId());
        
        if (CollectionUtils.isEmpty(orders)) {
            return BuildOrdersResponse.builder()
                    .certificateQuantity(0)
                    .existingOrders(List.of())
                    .newOrders(List.of())
                    .build();
        }
        List<Integer> existingOrders = new ArrayList<>();
        List<TechOrdersResponse> newOrders = new ArrayList<>();

        processOrders(orders, existingOrders, newOrders);
        publishNewOrders(newOrders);

        return buildResponse(existingOrders, newOrders);
    }

    private ProductEntity getOrCreateProduct(TechOrdersResponse sampleOrder) {
        return productRepository.findByProductId(sampleOrder.getProductId())
                .orElseGet(() -> createAndSaveProduct(sampleOrder));
    }

    private ProductEntity createAndSaveProduct(TechOrdersResponse order) {
        ProductEntity newProduct = new ProductEntity();
        newProduct.setProductId(order.getProductId());
        newProduct.setProductName(order.getProductName());
        newProduct.setCertificateDetails(order.getCertificateDetails());
        newProduct.setCertificateLogo(order.getCertificateLogo());
        newProduct.setCertificateBackground(order.getCertificateBackground());
        newProduct.setCheckinLatitude(order.getCheckinLatitude());
        newProduct.setCheckinLongitude(order.getCheckinLongitude());
        newProduct.setTimeCheckin(order.getTimeCheckin());
        return productRepository.save(newProduct);
    }

    private void processOrders(List<TechOrdersResponse> orders,
                             List<Integer> existingOrders, List<TechOrdersResponse> newOrders) {
        for (TechOrdersResponse order : orders) {
            ProductEntity product = getOrCreateProduct(order);
            ParticipantEntity participant = getOrCreateParticipant(order);
            processOrder(order, product, participant, existingOrders, newOrders);
        }
    }

    private ParticipantEntity getOrCreateParticipant(TechOrdersResponse order) {
        return participantRespository.findByEmail(order.getEmail())
                .orElseGet(() -> createAndSaveParticipant(order));
    }

    private ParticipantEntity createAndSaveParticipant(TechOrdersResponse order) {
        ParticipantEntity newParticipant = new ParticipantEntity();
        newParticipant.setFirstName(order.getFirstName());
        newParticipant.setLastName(order.getLastName());
        newParticipant.setEmail(order.getEmail());
        newParticipant.setPhone(order.getPhone());
        newParticipant.setCpf(order.getCpf());
        newParticipant.setCity(order.getCity());
        return participantRespository.save(newParticipant);
    }

    private void processOrder(TechOrdersResponse order, ProductEntity product, ParticipantEntity participant,
                              List<Integer> existingOrders, List<TechOrdersResponse> newOrders) {

        Optional<OrderEntity> existingOrder = orderRepository.findByOrderId(order.getOrderId());
        if (existingOrder.isPresent()) {
            existingOrders.add(order.getOrderId());

        } else {
            createAndSaveOrder(order, product, participant);
            newOrders.add(order);
        }

    }

    private void createAndSaveOrder(TechOrdersResponse order, ProductEntity product, ParticipantEntity participant) {
        OrderEntity newOrder = new OrderEntity();
        newOrder.setOrderId(order.getOrderId());
        newOrder.setProduct(product);
        newOrder.setParticipant(participant);
        newOrder.setOrderDate(LocalDateTime.parse(order.getOrderDate(), DATE_FORMATTER));
        orderRepository.save(newOrder);
    }

    private void publishNewOrders(List<TechOrdersResponse> newOrders) {
        if (CollectionUtils.isEmpty(newOrders)) {
            return;
        }
        orderEventPublisher.publishOrderCreatedEvent(newOrders);
    }

    private BuildOrdersResponse buildResponse(List<Integer> existingOrders,
                                            List<TechOrdersResponse> newOrders) {
        return BuildOrdersResponse.builder()
                .certificateQuantity(newOrders.size())
                .existingOrders(existingOrders)
                .newOrders(newOrders.stream().map(TechOrdersResponse::getOrderId).toList())
                .build();
    }
}
