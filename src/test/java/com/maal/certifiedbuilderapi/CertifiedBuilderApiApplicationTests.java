package com.maal.certifiedbuilderapi;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "url.service.tech=http://localhost:8080" // Propriedade correta para o Feign
})
class CertifiedBuilderApiApplicationTests {

    @MockBean
    private SqsTemplate sqsTemplate;

    @Test
    void contextLoads() {
    }
}
