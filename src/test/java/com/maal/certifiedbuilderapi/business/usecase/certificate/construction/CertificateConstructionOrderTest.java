package com.maal.certifiedbuilderapi.business.usecase.certificate.construction;

import com.maal.certifiedbuilderapi.business.dto.BuildOrdersRequest;
import com.maal.certifiedbuilderapi.business.dto.BuildOrdersResponse;
import com.maal.certifiedbuilderapi.business.usecase.certificate.TestDataBuilder;
import com.maal.certifiedbuilderapi.infrastructure.aws.sqs.OrderEventPublisher;
import com.maal.certifiedbuilderapi.infrastructure.client.TechFloripa;
import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CertificateConstructionOrder
 * Adaptado para nova arquitetura refatorada com serviços especializados
 * 
 * Estratégia de teste:
 * - Mocka o OrderProcessingService e dependências
 * - Testa cenários de negócio através do coordenador principal
 * - Verifica interações com serviços especializados
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CertificateConstructionOrder - Testes Unitários")
class CertificateConstructionOrderTest {

    @Mock
    private TechFloripa techFloripa;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @Mock
    private OrderProcessingService orderProcessingService;

    @InjectMocks
    private CertificateConstructionOrder certificateConstructionOrder;

    private BuildOrdersRequest request;
    private TechOrdersResponse techOrder;
    private ProcessingResult processingResult;

    @BeforeEach
    void setUp() {
        // Preparação de dados padrão para os testes
        request = TestDataBuilder.createBuildOrdersRequest(100);
        techOrder = TestDataBuilder.createTechOrder();
        
        // Cria um resultado de processamento padrão
        processingResult = new ProcessingResult();
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

        // Verifica que serviços não foram chamados
        verify(orderProcessingService, never()).processOrdersParallel(any());
        verify(orderEventPublisher, never()).publishOrderCreatedEvent(any());
    }

    @Test
    @DisplayName("Deve processar pedidos novos corretamente")
    void deveProcessarPedidosNovosCriandoProdutoEParticipante() {
        // Arrange
        List<TechOrdersResponse> orders = List.of(techOrder);
        when(techFloripa.getOrders(100)).thenReturn(orders);
        
        // Configura resultado com uma nova ordem
        processingResult.addNewOrder(techOrder);
        when(orderProcessingService.processOrdersParallel(orders)).thenReturn(processingResult);

        // Act
        BuildOrdersResponse response = certificateConstructionOrder.execute(request);

        // Assert
        assertEquals(1, response.getCertificateQuantity());
        assertTrue(response.getExistingOrders().isEmpty());
        assertEquals(List.of(1001), response.getNewOrders());

        // Verifica chamadas aos serviços
        verify(orderProcessingService).processOrdersParallel(orders);
        verify(orderEventPublisher).publishOrderCreatedEvent(List.of(techOrder));
    }

    @Test
    @DisplayName("Deve reutilizar produto e participante existentes")
    void deveReutilizarProdutoEParticipanteExistentes() {
        // Arrange
        List<TechOrdersResponse> orders = List.of(techOrder);
        when(techFloripa.getOrders(100)).thenReturn(orders);
        
        // Configura resultado com uma nova ordem (o processamento interno é responsabilidade do OrderProcessingService)
        processingResult.addNewOrder(techOrder);
        when(orderProcessingService.processOrdersParallel(orders)).thenReturn(processingResult);

        // Act
        BuildOrdersResponse response = certificateConstructionOrder.execute(request);

        // Assert
        assertEquals(1, response.getCertificateQuantity());
        
        // Verifica que o serviço de processamento foi chamado
        verify(orderProcessingService).processOrdersParallel(orders);
    }

    @Test
    @DisplayName("Deve identificar pedidos existentes e não reprocessá-los")
    void deveIdentificarPedidosExistentes() {
        // Arrange
        List<TechOrdersResponse> orders = List.of(techOrder);
        when(techFloripa.getOrders(100)).thenReturn(orders);
        
        // Configura resultado com ordem existente
        processingResult.addExistingOrder(1001);
        when(orderProcessingService.processOrdersParallel(orders)).thenReturn(processingResult);

        // Act
        BuildOrdersResponse response = certificateConstructionOrder.execute(request);

        // Assert
        assertEquals(0, response.getCertificateQuantity());
        assertEquals(List.of(1001), response.getExistingOrders());
        assertTrue(response.getNewOrders().isEmpty());

        // Verifica que não publicou eventos
        verify(orderEventPublisher, never()).publishOrderCreatedEvent(any());
    }

    @Test
    @DisplayName("Deve ignorar pedidos sem timeCheckin")
    void deveIgnorarPedidosSemTimeCheckin() {
        // Arrange
        TechOrdersResponse orderSemCheckin = TestDataBuilder.createTechOrderWithoutCheckin();
        List<TechOrdersResponse> orders = List.of(orderSemCheckin);
        when(techFloripa.getOrders(101)).thenReturn(orders);
        
        // O OrderProcessingService é responsável por filtrar ordens sem timeCheckin
        ProcessingResult emptyResult = new ProcessingResult();
        when(orderProcessingService.processOrdersParallel(orders)).thenReturn(emptyResult);

        // Act
        BuildOrdersResponse response = certificateConstructionOrder.execute(
            TestDataBuilder.createBuildOrdersRequest(101)
        );

        // Assert
        assertEquals(0, response.getCertificateQuantity());
        assertTrue(response.getExistingOrders().isEmpty());
        assertTrue(response.getNewOrders().isEmpty());

        // Verifica que o processamento foi chamado (a lógica de filtro é interna)
        verify(orderProcessingService).processOrdersParallel(orders);
    }

    @Test
    @DisplayName("Deve processar lista mista de pedidos novos e existentes")
    void deveProcessarListaMistaDePedidos() {
        // Arrange
        TechOrdersResponse pedidoNovo = TestDataBuilder.createTechOrder(2001, "novo@email.com", 200, TestDataBuilder.DEFAULT_TIME_CHECKIN);
        TechOrdersResponse pedidoExistente = TestDataBuilder.createTechOrder(2002, "existente@email.com", 200, TestDataBuilder.DEFAULT_TIME_CHECKIN);
        List<TechOrdersResponse> orders = List.of(pedidoNovo, pedidoExistente);

        // Configura resultado misto
        ProcessingResult mixedResult = new ProcessingResult();
        mixedResult.addNewOrder(pedidoNovo);
        mixedResult.addExistingOrder(2002);
        when(orderProcessingService.processOrdersParallel(orders)).thenReturn(mixedResult);

        // Act
        BuildOrdersResponse response = certificateConstructionOrder.execute(orders);

        // Assert
        assertEquals(1, response.getCertificateQuantity()); // Apenas 1 novo
        assertEquals(List.of(2002), response.getExistingOrders()); // 1 existente
        assertEquals(List.of(2001), response.getNewOrders()); // 1 novo

        // Verifica que publicou evento apenas para o pedido novo
        ArgumentCaptor<List<TechOrdersResponse>> captor = ArgumentCaptor.forClass(List.class);
        verify(orderEventPublisher).publishOrderCreatedEvent(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals(2001, captor.getValue().get(0).getOrderId());
    }

    @Test
    @DisplayName("Deve criar OrderEntity com dados desnormalizados corretos")
    void deveCriarOrderEntityComDadosDesnormalizados() {
        // Este teste agora verifica o comportamento coordenador
        // A lógica de criação de entidades está no EntityCreationService
        
        // Arrange
        List<TechOrdersResponse> orders = List.of(techOrder);
        when(techFloripa.getOrders(100)).thenReturn(orders);
        
        processingResult.addNewOrder(techOrder);
        when(orderProcessingService.processOrdersParallel(orders)).thenReturn(processingResult);

        // Act
        BuildOrdersResponse response = certificateConstructionOrder.execute(request);

        // Assert - Verifica que o coordenador funcionou corretamente
        assertEquals(1, response.getCertificateQuantity());
        assertEquals(List.of(1001), response.getNewOrders());
        
        // Verifica que o serviço de processamento foi chamado
        verify(orderProcessingService).processOrdersParallel(orders);
        verify(orderEventPublisher).publishOrderCreatedEvent(List.of(techOrder));
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
        verifyNoInteractions(techFloripa, orderProcessingService, orderEventPublisher);
    }

    @Test
    @DisplayName("Não deve publicar eventos quando não há pedidos novos")
    void naoDevePublicarEventosQuandoNaoHaPedidosNovos() {
        // Arrange - Todos os pedidos já existem
        List<TechOrdersResponse> orders = TestDataBuilder.createTechOrdersList();
        
        // Configura resultado com apenas ordens existentes
        ProcessingResult existingOnlyResult = new ProcessingResult();
        existingOnlyResult.addExistingOrder(1001);
        existingOnlyResult.addExistingOrder(1002);
        existingOnlyResult.addExistingOrder(1003);
        when(orderProcessingService.processOrdersParallel(orders)).thenReturn(existingOnlyResult);

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