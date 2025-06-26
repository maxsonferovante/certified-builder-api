package com.maal.certifiedbuilderapi.config;

import com.maal.certifiedbuilderapi.infrastructure.client.TechFloripa;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@TestConfiguration
@EnableFeignClients(clients = TechFloripa.class)
@Profile("test")
public class FeignConfig {

    @Bean
    @Primary
    public TechFloripa techFloripa() {
        return mock(TechFloripa.class);
    }
} 