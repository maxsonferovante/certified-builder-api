package com.maal.certifiedbuilderapi.infrastructure.aws.sqs.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.swing.text.StyledEditorKit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    @JsonProperty("order_id")
    private Integer orderId;

    @JsonProperty("product_id")
    private Integer  productId;

    @JsonProperty("product_name")
    private String productName;

    private String email;

    @JsonProperty("certificate_key")
    private String certificateKey;

    private Boolean success;
}
