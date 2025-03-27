package com.maal.certifiedbuilderapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CertifiedBuilderApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CertifiedBuilderApiApplication.class, args);
    }

}
