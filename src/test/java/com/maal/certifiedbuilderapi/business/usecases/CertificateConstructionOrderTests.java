package com.maal.certifiedbuilderapi.business.usecases;

import com.maal.certifiedbuilderapi.business.dto.BuildOrdersResponse;
import com.maal.certifiedbuilderapi.business.usecase.certificate.CertificateConstructionOrder;
import com.maal.certifiedbuilderapi.config.AwsTestConfig;
import com.maal.certifiedbuilderapi.config.FeignConfig;
import com.maal.certifiedbuilderapi.config.TestConfig;
import com.maal.certifiedbuilderapi.infrastructure.aws.sqs.OrderEventPublisher;
import com.maal.certifiedbuilderapi.infrastructure.client.TechFloripa;
import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ParticipantRespository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import({TestConfig.class, FeignConfig.class, AwsTestConfig.class})
@ActiveProfiles("test")
class CertificateConstructionOrderTests {

    @Autowired
    private CertificateConstructionOrder certificateConstructionOrder;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ParticipantRespository participantRespository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TechFloripa techFloripa;

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    @Test
    @DisplayName("Should process orders and create certificates with list of orders")
    void shouldProcessOrdersAndCreateCertificatesWithListOfOrders() {
        // Given
        TechOrdersResponse techOrdersResponse1 = createSampleTechOrderResponse();
        TechOrdersResponse techOrdersResponse2 = createSampleTechOrderResponse();
        List<TechOrdersResponse> techOrdersResponses = List.of(techOrdersResponse1, techOrdersResponse2);

        // Configurar mocks
        when(productRepository.findByProductId(any())).thenReturn(Optional.empty());
        when(participantRespository.findByEmail(any())).thenReturn(Optional.empty());
        when(orderRepository.findByOrderId(any())).thenReturn(Optional.empty());

        when(productRepository.save(any())).thenReturn(null);
        when(participantRespository.save(any())).thenReturn(null);
        when(orderRepository.save(any())).thenReturn(null);

        // When
        BuildOrdersResponse result = certificateConstructionOrder.execute(techOrdersResponses);

        // Then
        assertEquals(2, result.getCertificateQuantity());
        assertEquals(0, result.getExistingOrders().size());
        assertEquals(2, result.getNewOrders().size());

        // Verificar interações
        verify(orderEventPublisher, times(1)).publishOrderCreatedEvent(techOrdersResponses);
    }

    @Test
    @DisplayName("Should process orders and create certificates")
    void processOrders() {
        // Given
        TechOrdersResponse techOrdersResponse1 = createSampleTechOrderResponse();
        List<TechOrdersResponse> techOrdersResponses = List.of(techOrdersResponse1);

        // Configurar mocks
        when(productRepository.findByProductId(any())).thenReturn(Optional.empty());
        when(participantRespository.findByEmail(any())).thenReturn(Optional.empty());
        when(orderRepository.findByOrderId(any())).thenReturn(Optional.empty());

        when(productRepository.save(any())).thenReturn(null);
        when(participantRespository.save(any())).thenReturn(null);
        when(orderRepository.save(any())).thenReturn(null);

        // When
        BuildOrdersResponse result = certificateConstructionOrder.execute(techOrdersResponses);

        // Then
        assertEquals(1, result.getCertificateQuantity());
        assertEquals(0, result.getExistingOrders().size());
        assertEquals(1, result.getNewOrders().size());

        // Verificar interações
        verify(orderEventPublisher, times(1)).publishOrderCreatedEvent(techOrdersResponses);
        verify(productRepository, times(1)).save(any());
        verify(participantRespository, times(1)).save(any());
        verify(orderRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should return empty response when no orders are provided")
    void processOrdersWithEmptyList() {
        // Given
        List<TechOrdersResponse> techOrdersResponses = List.of();

        // When
        BuildOrdersResponse result = certificateConstructionOrder.execute(techOrdersResponses);

        // Then
        assertEquals(0, result.getCertificateQuantity());
        assertEquals(0, result.getExistingOrders().size());
        assertEquals(0, result.getNewOrders().size());

        // Verificar interações
        verify(orderEventPublisher, never()).publishOrderCreatedEvent(any());
    }

    @Test
    @DisplayName("Should return empty response when no orders are found")
    void processOrdersWithNoOrdersFound() {
        // Given
        List<TechOrdersResponse> techOrdersResponses = List.of();

        // When
        BuildOrdersResponse result = certificateConstructionOrder.execute(techOrdersResponses);

        // Then
        assertEquals(0, result.getCertificateQuantity());
        assertEquals(0, result.getExistingOrders().size());
        assertEquals(0, result.getNewOrders().size());

        // Verificar interações
        verify(orderEventPublisher, never()).publishOrderCreatedEvent(any());
    }

    private TechOrdersResponse createSampleTechOrderResponse() {
        TechOrdersResponse response = new TechOrdersResponse();
        response.setOrderId(1001);
        response.setFirstName("John");
        response.setLastName("Doe");
        response.setEmail("john.doe@example.com");
        response.setPhone("(11) 99999-9999");
        response.setCpf("123.456.789-00");
        response.setCity("São Paulo");
        response.setProductId(500);
        response.setProductName("Tech Conference 2024");
        response.setCertificateDetails("Test certificate details");
        response.setCertificateLogo("https://example.com/logo.png");
        response.setCertificateBackground("https://example.com/bg.png");
        response.setOrderDate("2024-03-15 14:30:00");
        response.setCheckinLatitude("-23.550520");
        response.setCheckinLongitude("-46.633308");
        response.setTimeCheckin("2024-03-15 14:35:00");
        return response;
    }
}