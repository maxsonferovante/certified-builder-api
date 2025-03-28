package com.maal.certifiedbuilderapi.business.usecases;

import com.maal.certifiedbuilderapi.business.dto.BuildOrdersRequest;
import com.maal.certifiedbuilderapi.business.dto.BuildOrdersResponse;
import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import com.maal.certifiedbuilderapi.domain.entity.ParticipantEntity;
import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import com.maal.certifiedbuilderapi.infrastructure.aws.sqs.OrderEventPublisher;
import com.maal.certifiedbuilderapi.infrastructure.client.TechFloripa;
import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRespository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ParticipantRespository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CertificateConstructionOrder {

    private final TechFloripa techFloripa;
    private final OrderRespository orderRespository;
    private final ParticipantRespository participantRespository;
    private final ProductRepository productRepository;

    private final OrderEventPublisher orderEventPublisher;

    public BuildOrdersResponse execute(BuildOrdersRequest request) {

        List<TechOrdersResponse> orders = techFloripa.getOrders(request.getProductId());

        // Cria ou recupera o produto uma única vez fora do loop
        ProductEntity productEntity = productRepository.findByProductId(request.getProductId())
                .orElseGet(() -> {
                    TechOrdersResponse sampleOrder = orders.getFirst(); // Pega os dados do primeiro pedido
                    ProductEntity newProduct = new ProductEntity();
                    newProduct.setProductId(sampleOrder.getProductId());
                    newProduct.setProductName(sampleOrder.getProductName());
                    newProduct.setCertificateDetails(sampleOrder.getCertificateDetails());
                    newProduct.setCertificateLogo(sampleOrder.getCertificateLogo());
                    newProduct.setCertificateBackground(sampleOrder.getCertificateBackground());
                    newProduct.setCheckinLatitude(sampleOrder.getCheckinLatitude());
                    newProduct.setCheckinLongitude(sampleOrder.getCheckinLongitude());
                    newProduct.setTimeCheckin(sampleOrder.getTimeCheckin());
                    return productRepository.save(newProduct);
                });

        List<Integer> existingOrders = new ArrayList<>();
        List<TechOrdersResponse> newOrders = new ArrayList<>();

        for (TechOrdersResponse order : orders) {
            // Cria ou recupera o participante
            ParticipantEntity participantEntity = participantRespository.findByEmail(order.getEmail())
                    .orElseGet(() -> {
                        ParticipantEntity newParticipant = new ParticipantEntity();
                        newParticipant.setFirstName(order.getFirstName());
                        newParticipant.setLastName(order.getLastName());
                        newParticipant.setEmail(order.getEmail());
                        newParticipant.setPhone(order.getPhone());
                        newParticipant.setCpf(order.getCpf());
                        newParticipant.setCity(order.getCity());
                        return participantRespository.save(newParticipant);
                    });

            // Verifica se a ordem já existe
            Optional<OrderEntity> existingOrder = orderRespository.findByOrderId(order.getOrderId());

            if (existingOrder.isPresent()) {
                existingOrders.add(order.getOrderId());
            } else {
                OrderEntity newOrder = new OrderEntity();
                newOrder.setOrderId(order.getOrderId());
                newOrder.setProduct(productEntity);
                newOrder.setParticipant(participantEntity);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                newOrder.setOrderDate(LocalDateTime.parse(order.getOrderDate(), formatter));

                orderRespository.save(newOrder);
                newOrders.add(order);
            }
        }

        // envia as novas orders para geração dos certificados
        orderEventPublisher.publishOrderCreatedEvent(newOrders);

        // Retorno montado com os dados necessários
        return BuildOrdersResponse.builder()
                .productId(productEntity.getProductId())
                .productName(productEntity.getProductName())
                .certificateQuantity(newOrders.size()) // Contagem das ordens criadas
                .existingOrders(existingOrders) // Lista de ordens já existentes
                .newOrders(newOrders.stream().map(TechOrdersResponse::getOrderId).toList())           // Lista de novas ordens
                .build();
    }
}
