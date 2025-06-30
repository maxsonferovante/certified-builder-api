package com.maal.certifiedbuilderapi.business.usecase.certificate;

import com.maal.certifiedbuilderapi.business.dto.BuildOrdersRequest;
import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import com.maal.certifiedbuilderapi.domain.entity.ParticipantEntity;
import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Builder de dados para testes do CertificateConstructionOrder
 * Centraliza a criação de objetos de teste com dados consistentes
 */
public class TestDataBuilder {

    public static final String DEFAULT_ORDER_DATE = "2023-12-15 10:30:00";
    public static final String DEFAULT_TIME_CHECKIN = "2023-12-15 08:00:00";

    /**
     * Cria um TechOrdersResponse com dados padrão para testes
     */
    public static TechOrdersResponse createTechOrder() {
        return createTechOrder(1001, "joao@email.com", 100, DEFAULT_TIME_CHECKIN);
    }

    /**
     * Cria um TechOrdersResponse customizado para testes específicos
     */
    public static TechOrdersResponse createTechOrder(Integer orderId, String email, Integer productId, String timeCheckin) {
        TechOrdersResponse order = new TechOrdersResponse();
        order.setOrderId(orderId);
        order.setFirstName("João");
        order.setLastName("Silva");
        order.setEmail(email);
        order.setPhone("11999999999");
        order.setCpf("12345678901");
        order.setCity("São Paulo");
        order.setProductId(productId);
        order.setProductName("Curso de Java");
        order.setCertificateDetails("Certificado de conclusão do curso");
        order.setCertificateLogo("logo.png");
        order.setCertificateBackground("background.jpg");
        order.setOrderDate(DEFAULT_ORDER_DATE);
        order.setCheckinLatitude("-23.5505");
        order.setCheckinLongitude("-46.6333");
        order.setTimeCheckin(timeCheckin);
        return order;
    }

    /**
     * Cria um TechOrdersResponse sem timeCheckin (deve ser ignorado)
     */
    public static TechOrdersResponse createTechOrderWithoutCheckin() {
        return createTechOrder(1002, "maria@email.com", 101, "");
    }

    /**
     * Cria uma lista de TechOrdersResponse para testes com múltiplos pedidos
     */
    public static List<TechOrdersResponse> createTechOrdersList() {
        return List.of(
            createTechOrder(1001, "joao@email.com", 100, DEFAULT_TIME_CHECKIN),
            createTechOrder(1002, "maria@email.com", 101, DEFAULT_TIME_CHECKIN),
            createTechOrder(1003, "pedro@email.com", 100, DEFAULT_TIME_CHECKIN)
        );
    }

    /**
     * Cria uma ProductEntity para testes
     */
    public static ProductEntity createProduct(Integer productId) {
        ProductEntity product = new ProductEntity();
        product.setId("product-uuid-" + productId);
        product.setProductId(productId);
        product.setProductName("Curso de Java");
        product.setCertificateDetails("Certificado de conclusão do curso");
        product.setCertificateLogo("logo.png");
        product.setCertificateBackground("background.jpg");
        product.setCheckinLatitude("-23.5505");
        product.setCheckinLongitude("-46.6333");
        product.setTimeCheckin(DEFAULT_TIME_CHECKIN);
        return product;
    }

    /**
     * Cria uma ParticipantEntity para testes
     */
    public static ParticipantEntity createParticipant(String email) {
        ParticipantEntity participant = new ParticipantEntity();
        participant.setId("participant-uuid-" + email.hashCode());
        participant.setFirstName("João");
        participant.setLastName("Silva");
        participant.setEmail(email);
        participant.setPhone("11999999999");
        participant.setCpf("12345678901");
        participant.setCity("São Paulo");
        return participant;
    }

    /**
     * Cria uma OrderEntity para testes
     */
    public static OrderEntity createOrder(Integer orderId) {
        OrderEntity order = new OrderEntity();
        order.setId("order-uuid-" + orderId);
        order.setOrderId(orderId);
        order.setOrderDateFromLocalDateTime(LocalDateTime.parse(DEFAULT_ORDER_DATE.replace(" ", "T")));
        
        // Dados do produto desnormalizados
        order.setProductId(100);
        order.setProductName("Curso de Java");
        order.setCertificateDetails("Certificado de conclusão do curso");
        order.setCertificateLogo("logo.png");
        order.setCertificateBackground("background.jpg");
        order.setCheckinLatitude("-23.5505");
        order.setCheckinLongitude("-46.6333");
        order.setTimeCheckin(DEFAULT_TIME_CHECKIN);
        
        // Dados do participante desnormalizados
        order.setParticipantEmail("joao@email.com");
        order.setParticipantFirstName("João");
        order.setParticipantLastName("Silva");
        order.setParticipantCpf("12345678901");
        order.setParticipantPhone("11999999999");
        order.setParticipantCity("São Paulo");
        
        return order;
    }

    /**
     * Cria um BuildOrdersRequest para testes
     */
    public static BuildOrdersRequest createBuildOrdersRequest(Integer productId) {
        return new BuildOrdersRequest(productId);
    }
} 