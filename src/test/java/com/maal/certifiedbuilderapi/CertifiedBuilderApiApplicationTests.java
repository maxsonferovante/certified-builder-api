package com.maal.certifiedbuilderapi;

import com.maal.certifiedbuilderapi.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
class CertifiedBuilderApiApplicationTests {

    @Test
    void contextLoads() {
    }

}