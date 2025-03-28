package com.maal.certifiedbuilderapi.business.dto;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteProductResponse {
    Integer productId;
    LocalDateTime deletedAt;
}
