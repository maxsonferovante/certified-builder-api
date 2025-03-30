package com.maal.certifiedbuilderapi;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "service.url=http://localhost:8080", // Valor fixo para o Feign
        "spring.cloud.aws.sqs.enabled=false" // Tenta desativar o SQS
})
class CertifiedBuilderApiApplicationTests {

    @MockBean
    private SqsTemplate sqsTemplate; // Mock do SqsTemplate

    @Test
    void contextLoads() {
    }
}