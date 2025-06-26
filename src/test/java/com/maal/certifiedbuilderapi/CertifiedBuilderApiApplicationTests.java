package com.maal.certifiedbuilderapi;

import com.maal.certifiedbuilderapi.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestConfig.class)
class CertifiedBuilderApiApplicationTests {

    @Test
    void contextLoads() {
    }

}