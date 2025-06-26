package com.maal.certifiedbuilderapi;

import com.maal.certifiedbuilderapi.config.AwsTestConfig;
import com.maal.certifiedbuilderapi.config.FeignConfig;
import com.maal.certifiedbuilderapi.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import({TestConfig.class, FeignConfig.class, AwsTestConfig.class})
@ActiveProfiles("test")
class CertifiedBuilderApiApplicationTests {

    @Test
    void contextLoads() {
    }

}