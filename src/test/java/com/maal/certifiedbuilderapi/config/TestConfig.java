package com.maal.certifiedbuilderapi.config;

import com.maal.certifiedbuilderapi.infrastructure.repository.CertificateRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ParticipantRespository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ProductRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Configuração de teste que substitui os repositórios reais por mocks
 * Evita a necessidade de configurar DynamoDB local para testes unitários
 */
@TestConfiguration
public class TestConfig {

    /**
     * Mock do CertificateRepository para testes
     */
    @Bean
    @Primary
    public CertificateRepository certificateRepository() {
        return org.mockito.Mockito.mock(CertificateRepository.class);
    }

    /**
     * Mock do OrderRepository para testes
     */
    @Bean
    @Primary
    public OrderRepository orderRepository() {
        return org.mockito.Mockito.mock(OrderRepository.class);
    }

    /**
     * Mock do ParticipantRepository para testes
     */
    @Bean
    @Primary
    public ParticipantRespository participantRepository() {
        return org.mockito.Mockito.mock(ParticipantRespository.class);
    }

    /**
     * Mock do ProductRepository para testes
     */
    @Bean
    @Primary
    public ProductRepository productRepository() {
        return org.mockito.Mockito.mock(ProductRepository.class);
    }
} 