package com.maal.certifiedbuilderapi;

import com.maal.certifiedbuilderapi.infrastructure.aws.sqs.EventQueuesProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

// Disabling the Default Auto-Configuration
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})


//Finally, we need to use the @EnableConfigurationProperties annotation in a @Configuration annotated class, or the main Spring Application class, to let Spring Boot know we want to populate it with our application.yml properties
@EnableConfigurationProperties(EventQueuesProperties.class)
@EnableFeignClients
public class CertifiedBuilderApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CertifiedBuilderApiApplication.class, args);
    }

}
