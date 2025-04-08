package com.maal.certifiedbuilderapi.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateStatisticsResponse {
    private Integer productId;
    private String productName;
    private Integer totalCertificates;
    private Integer successfulCertificates;
    private Integer failedCertificates;
    private Integer pendingCertificates;
}


