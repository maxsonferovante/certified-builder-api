package com.maal.certifiedbuilderapi.business.dto;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecoverCertificatesResponse {
    private Boolean success;
    private String certificateId;
    private String certificateUrl;
    private LocalDateTime generetedDate;
    private Integer productId;
    private String productName;
    private Integer orderId;
    private LocalDateTime orderDate;
}
