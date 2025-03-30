package com.maal.certifiedbuilderapi;

import com.maal.certifiedbuilderapi.infrastructure.aws.sqs.OrderEventPublisher;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "url.service.tech=http://localhost:8080", // Propriedade correta para o Feign
        "spring.data.mongodb.uri=mongodb://myuser:mypassword@localhost:27017/admin",
        "spring.cloud.aws.sqs.enabled=false" // Desativa o SQS
})
class CertifiedBuilderApiApplicationTests {

    @MockBean
    private SqsTemplate sqsTemplate;

    @Test
    void contextLoads() {
    }
}
