package com.maal.certifiedbuilderapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class TransactionConfig {
    // Transaction management is enabled via annotations
    // Additional transaction-specific beans can be added here if needed
} 