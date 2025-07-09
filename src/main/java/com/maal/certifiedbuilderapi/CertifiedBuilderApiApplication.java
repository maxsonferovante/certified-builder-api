package com.maal.certifiedbuilderapi;

import com.maal.certifiedbuilderapi.infrastructure.aws.sqs.EventQueuesProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

// Configuração da aplicação Spring Boot - excluindo auto-configurações de segurança
@SpringBootApplication(exclude = {
    SecurityAutoConfiguration.class, 
    UserDetailsServiceAutoConfiguration.class
})
@EnableFeignClients
@EnableRetry    // Habilita suporte a @Retryable para resiliência
@EnableAsync    // Habilita suporte a @Async para processamento assíncrono
public class CertifiedBuilderApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CertifiedBuilderApiApplication.class, args);
    }

    /**
     * Configuração condicional para carregar EventQueuesProperties apenas em ambientes não-test
     */
    @Configuration
    @Profile("!test")
    @EnableConfigurationProperties(EventQueuesProperties.class)
    static class ProductionConfiguration {
    }
}
