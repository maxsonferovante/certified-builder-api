package com.maal.certifiedbuilderapi.business.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildOrdersResponse {
    Integer productId;
    String productName;
    Integer certificateQuantity;
}
