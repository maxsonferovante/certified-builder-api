package com.maal.certifiedbuilderapi.business.usecase.certificate;

import com.maal.certifiedbuilderapi.business.dto.BuildOrdersRequest;
import com.maal.certifiedbuilderapi.business.dto.BuildOrdersResponse;
import com.maal.certifiedbuilderapi.domain.entity.OrderEntity;
import com.maal.certifiedbuilderapi.domain.entity.ParticipantEntity;
import com.maal.certifiedbuilderapi.domain.entity.ProductEntity;
import com.maal.certifiedbuilderapi.infrastructure.aws.sqs.OrderEventPublisher;
import com.maal.certifiedbuilderapi.infrastructure.client.TechFloripa;
import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ParticipantRespository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CertificateConstructionOrder
 * 
 * Estratégia de teste:
 * - Mocka todas as dependências externas
 * - Testa cenários de negócio específicos
 * - Verifica interações com repositórios e serviços
 * - Valida dados desnormalizados no DynamoDB
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CertificateConstructionOrder - Testes Unitários")
class CertificateConstructionOrderTest {

    @Mock
    private TechFloripa techFloripa;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ParticipantRespository participantRespository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private CertificateConstructionOrder certificateConstructionOrder;

    private BuildOrdersRequest request;
    private TechOrdersResponse techOrder;
    private ProductEntity existingProduct;
    private ParticipantEntity existingParticipant;

    @BeforeEach
    void setUp() {
        // Preparação de dados padrão para os testes
        request = TestDataBuilder.createBuildOrdersRequest(100);
        techOrder = TestDataBuilder.createTechOrder();
        existingProduct = TestDataBuilder.createProduct(100);
        existingParticipant = TestDataBuilder.createParticipant("joao@email.com");
    }

    @Test
    @DisplayName("Deve retornar resposta vazia quando TechFloripa não retorna pedidos")
    void deveRetornarRespostaVaziaQuandoNaoHaPedidos() {
        // Arrange - Configuração do cenário
        when(techFloripa.getOrders(100)).thenReturn(List.of());

        // Act - Execução do método testado
        BuildOrdersResponse response = certificateConstructionOrder.execute(request);

        // Assert - Verificação dos resultados
        assertNotNull(response);
        assertEquals(0, response.getCertificateQuantity());
        assertTrue(response.getExistingOrders().isEmpty());
        assertTrue(response.getNewOrders().isEmpty());

        // Verifica que repositórios não foram chamados
        verify(orderRepository, never()).save(any());
        verify(orderEventPublisher, never()).publishOrderCreatedEvent(any());
    }

    @Test
    @DisplayName("Deve processar pedidos novos corretamente criando produto e participante")
    void deveProcessarPedidosNovosCriandoProdutoEParticipante() {
        // Arrange
        List<TechOrdersResponse> orders = List.of(techOrder);
        when(techFloripa.getOrders(100)).thenReturn(orders);
        
        // Simula que produto e participante não existem
        when(productRepository.findByProductId(100)).thenReturn(Optional.empty());
        when(participantRespository.findByEmail("joao@email.com")).thenReturn(Optional.empty());
        when(orderRepository.findByOrderId(1001)).thenReturn(Optional.empty());
        
        // Simula salvamento de novos registros
        when(productRepository.save(any(ProductEntity.class))).thenReturn(existingProduct);
        when(participantRespository.save(any(ParticipantEntity.class))).thenReturn(existingParticipant);
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(TestDataBuilder.createOrder(1001));

        // Act
        BuildOrdersResponse response = certificateConstructionOrder.execute(request);

        // Assert
        assertEquals(1, response.getCertificateQuantity());
        assertTrue(response.getExistingOrders().isEmpty());
        assertEquals(List.of(1001), response.getNewOrders());

        // Verifica criação de produto
        verify(productRepository).save(argThat(product -> 
            product.getProductId().equals(100) && 
            product.getProductName().equals("Curso de Java")
        ));

        // Verifica criação de participante
        verify(participantRespository).save(argThat(participant -> 
            participant.getEmail().equals("joao@email.com") && 
            participant.getFirstName().equals("João")
        ));

        // Verifica criação de pedido
        verify(orderRepository).save(any(OrderEntity.class));

        // Verifica publicação de evento
        verify(orderEventPublisher).publishOrderCreatedEvent(orders);
    }

    @Test
    @DisplayName("Deve reutilizar produto e participante existentes")
    void deveReutilizarProdutoEParticipanteExistentes() {
        // Arrange
        List<TechOrdersResponse> orders = List.of(techOrder);
        when(techFloripa.getOrders(100)).thenReturn(orders);
        
        // Simula que produto e participante já existem
        when(productRepository.findByProductId(100)).thenReturn(Optional.of(existingProduct));
        when(participantRespository.findByEmail("joao@email.com")).thenReturn(Optional.of(existingParticipant));
        when(orderRepository.findByOrderId(1001)).thenReturn(Optional.empty());
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(TestDataBuilder.createOrder(1001));

        // Act
        BuildOrdersResponse response = certificateConstructionOrder.execute(request);

        // Assert
        assertEquals(1, response.getCertificateQuantity());

        // Verifica que não criou novos produto e participante
        verify(productRepository, never()).save(any(ProductEntity.class));
        verify(participantRespository, never()).save(any(ParticipantEntity.class));

        // Mas criou o pedido
        verify(orderRepository).save(any(OrderEntity.class));
    }

    @Test
    @DisplayName("Deve identificar pedidos existentes e não reprocessá-los")
    void deveIdentificarPedidosExistentes() {
        // Arrange
        List<TechOrdersResponse> orders = List.of(techOrder);
        when(techFloripa.getOrders(100)).thenReturn(orders);
        
        // O código sempre verifica produto e participante primeiro
        when(productRepository.findByProductId(100)).thenReturn(Optional.of(existingProduct));
        when(participantRespository.findByEmail("joao@email.com")).thenReturn(Optional.of(existingParticipant));
        
        // Simula que o pedido já existe
        when(orderRepository.findByOrderId(1001)).thenReturn(Optional.of(TestDataBuilder.createOrder(1001)));

        // Act
        BuildOrdersResponse response = certificateConstructionOrder.execute(request);

        // Assert
        assertEquals(0, response.getCertificateQuantity());
        assertEquals(List.of(1001), response.getExistingOrders());
        assertTrue(response.getNewOrders().isEmpty());

        // Verifica que produto e participante não foram criados (reutilizou existentes)
        verify(productRepository, never()).save(any());
        verify(participantRespository, never()).save(any());
        // Verifica que não criou novo pedido
        verify(orderRepository, never()).save(any());
        verify(orderEventPublisher, never()).publishOrderCreatedEvent(any());
    }

    @Test
    @DisplayName("Deve ignorar pedidos sem timeCheckin")
    void deveIgnorarPedidosSemTimeCheckin() {
        // Arrange
        TechOrdersResponse orderSemCheckin = TestDataBuilder.createTechOrderWithoutCheckin();
        List<TechOrdersResponse> orders = List.of(orderSemCheckin);
        when(techFloripa.getOrders(101)).thenReturn(orders);

        // Act
        BuildOrdersResponse response = certificateConstructionOrder.execute(
            TestDataBuilder.createBuildOrdersRequest(101)
        );

        // Assert
        assertEquals(0, response.getCertificateQuantity());
        assertTrue(response.getExistingOrders().isEmpty());
        assertTrue(response.getNewOrders().isEmpty());

        // Verifica que nenhum repositório foi chamado
        verify(productRepository, never()).findByProductId(any());
        verify(participantRespository, never()).findByEmail(any());
        verify(orderRepository, never()).findByOrderId(any());
    }

    @Test
    @DisplayName("Deve processar lista mista de pedidos novos e existentes")
    void deveProcessarListaMistaDePedidos() {
        // Arrange
        TechOrdersResponse pedidoNovo = TestDataBuilder.createTechOrder(2001, "novo@email.com", 200, TestDataBuilder.DEFAULT_TIME_CHECKIN);
        TechOrdersResponse pedidoExistente = TestDataBuilder.createTechOrder(2002, "existente@email.com", 200, TestDataBuilder.DEFAULT_TIME_CHECKIN);
        List<TechOrdersResponse> orders = List.of(pedidoNovo, pedidoExistente);

        // Configura mocks
        when(productRepository.findByProductId(200)).thenReturn(Optional.of(TestDataBuilder.createProduct(200)));
        when(participantRespository.findByEmail("novo@email.com")).thenReturn(Optional.of(TestDataBuilder.createParticipant("novo@email.com")));
        when(participantRespository.findByEmail("existente@email.com")).thenReturn(Optional.of(TestDataBuilder.createParticipant("existente@email.com")));
        
        // Pedido novo não existe, pedido existente já existe
        when(orderRepository.findByOrderId(2001)).thenReturn(Optional.empty());
        when(orderRepository.findByOrderId(2002)).thenReturn(Optional.of(TestDataBuilder.createOrder(2002)));
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(TestDataBuilder.createOrder(2001));

        // Act
        BuildOrdersResponse response = certificateConstructionOrder.execute(orders);

        // Assert
        assertEquals(1, response.getCertificateQuantity()); // Apenas 1 novo
        assertEquals(List.of(2002), response.getExistingOrders()); // 1 existente
        assertEquals(List.of(2001), response.getNewOrders()); // 1 novo

        // Verifica que salvou apenas o pedido novo
        verify(orderRepository, times(1)).save(any(OrderEntity.class));
        
        // Verifica que publicou evento apenas para o pedido novo
        ArgumentCaptor<List<TechOrdersResponse>> captor = ArgumentCaptor.forClass(List.class);
        verify(orderEventPublisher).publishOrderCreatedEvent(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals(2001, captor.getValue().get(0).getOrderId());
    }

    @Test
    @DisplayName("Deve criar OrderEntity com dados desnormalizados corretos")
    void deveCriarOrderEntityComDadosDesnormalizados() {
        // Arrange
        List<TechOrdersResponse> orders = List.of(techOrder);
        when(techFloripa.getOrders(100)).thenReturn(orders);
        when(productRepository.findByProductId(100)).thenReturn(Optional.of(existingProduct));
        when(participantRespository.findByEmail("joao@email.com")).thenReturn(Optional.of(existingParticipant));
        when(orderRepository.findByOrderId(1001)).thenReturn(Optional.empty());
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(TestDataBuilder.createOrder(1001));

        // Act
        certificateConstructionOrder.execute(request);

        // Assert - Captura o OrderEntity salvo e verifica desnormalização
        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(orderCaptor.capture());
        
        OrderEntity savedOrder = orderCaptor.getValue();
        
        // Verifica dados básicos do pedido
        assertEquals(1001, savedOrder.getOrderId());
        assertNotNull(savedOrder.getOrderDate());
        
        // Verifica dados desnormalizados do produto
        assertEquals(100, savedOrder.getProductId());
        assertEquals("Curso de Java", savedOrder.getProductName());
        assertEquals("Certificado de conclusão do curso", savedOrder.getCertificateDetails());
        assertEquals("logo.png", savedOrder.getCertificateLogo());
        assertEquals("background.jpg", savedOrder.getCertificateBackground());
        assertEquals("-23.5505", savedOrder.getCheckinLatitude());
        assertEquals("-46.6333", savedOrder.getCheckinLongitude());
        assertEquals(TestDataBuilder.DEFAULT_TIME_CHECKIN, savedOrder.getTimeCheckin());
        
        // Verifica dados desnormalizados do participante
        assertEquals("joao@email.com", savedOrder.getParticipantEmail());
        assertEquals("João", savedOrder.getParticipantFirstName());
        assertEquals("Silva", savedOrder.getParticipantLastName());
        assertEquals("12345678901", savedOrder.getParticipantCpf());
        assertEquals("11999999999", savedOrder.getParticipantPhone());
        assertEquals("São Paulo", savedOrder.getParticipantCity());
    }

    @Test
    @DisplayName("Deve executar método com lista vazia de TechOrdersResponse")
    void deveExecutarComListaVaziaDeTechOrders() {
        // Act
        BuildOrdersResponse response = certificateConstructionOrder.execute(List.of());

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getCertificateQuantity());
        assertTrue(response.getExistingOrders().isEmpty());
        assertTrue(response.getNewOrders().isEmpty());

        // Verifica que nenhum serviço foi chamado
        verifyNoInteractions(techFloripa, orderRepository, productRepository, 
                            participantRespository, orderEventPublisher);
    }

    @Test
    @DisplayName("Não deve publicar eventos quando não há pedidos novos")
    void naoDevePublicarEventosQuandoNaoHaPedidosNovos() {
        // Arrange - Todos os pedidos já existem
        List<TechOrdersResponse> orders = TestDataBuilder.createTechOrdersList();
        when(orderRepository.findByOrderId(any())).thenReturn(Optional.of(TestDataBuilder.createOrder(1001)));

        // Act
        BuildOrdersResponse response = certificateConstructionOrder.execute(orders);

        // Assert
        assertEquals(0, response.getCertificateQuantity());
        assertEquals(3, response.getExistingOrders().size());
        assertTrue(response.getNewOrders().isEmpty());

        // Verifica que não publicou eventos
        verify(orderEventPublisher, never()).publishOrderCreatedEvent(any());
    }
} 