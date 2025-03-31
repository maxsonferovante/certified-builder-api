package com.maal.certifiedbuilderapi.business.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildOrdersResponse {
    private Integer certificateQuantity;
    private List<Integer> existingOrders;
    private List<Integer> newOrders;
}
