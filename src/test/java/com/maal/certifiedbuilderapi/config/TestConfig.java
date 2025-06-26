package com.maal.certifiedbuilderapi.config;

import com.maal.certifiedbuilderapi.business.usecase.certificate.CertificateConstructionOrder;
import com.maal.certifiedbuilderapi.business.usecase.certificate.DeleteProduct;
import com.maal.certifiedbuilderapi.business.usecase.certificate.GetCertificateStatistics;
import com.maal.certifiedbuilderapi.infrastructure.aws.s3.S3ClientCustomer;
import com.maal.certifiedbuilderapi.infrastructure.aws.sqs.OrderEventPublisher;
import com.maal.certifiedbuilderapi.infrastructure.client.TechFloripa;
import com.maal.certifiedbuilderapi.infrastructure.repository.CertificateRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.OrderRepository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ParticipantRespository;
import com.maal.certifiedbuilderapi.infrastructure.repository.ProductRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Configuração de teste completamente isolada
 * Não carrega configurações automáticas da aplicação, apenas mocks necessários
 */
@TestConfiguration
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@Profile("test")
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

    @Bean
    @Primary
    public OrderEventPublisher orderEventPublisher() {
        return org.mockito.Mockito.mock(OrderEventPublisher.class);
    }

    @Bean
    @Primary
    public S3ClientCustomer s3ClientCustomer() {
        return org.mockito.Mockito.mock(S3ClientCustomer.class);
    }

    @Bean
    public CertificateConstructionOrder certificateConstructionOrder(
        TechFloripa techFloripa,
        OrderRepository orderRepository,
        ParticipantRespository participantRepository,
        ProductRepository productRepository,
        OrderEventPublisher orderEventPublisher
    ) {
        return new CertificateConstructionOrder(
            techFloripa,
            orderRepository,
            participantRepository,
            productRepository,
            orderEventPublisher
        );
    }

    @Bean
    public DeleteProduct deleteProduct(
        OrderRepository orderRepository,
        ProductRepository productRepository,
        CertificateRepository certificateRepository,
        S3ClientCustomer s3ClientCustomer
    ) {
        return new DeleteProduct(
            orderRepository,
            productRepository,
            certificateRepository,
            s3ClientCustomer
        );
    }

    @Bean
    public GetCertificateStatistics getCertificateStatistics(
        OrderRepository orderRepository,
        CertificateRepository certificateRepository,
        ProductRepository productRepository
    ) {
        return new GetCertificateStatistics(
            orderRepository,
            certificateRepository,
            productRepository
        );
    }
} 