    package com.maal.certifiedbuilderapi.domain.event;

    import com.fasterxml.jackson.annotation.JsonProperty;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import lombok.AllArgsConstructor;

    import java.time.LocalDateTime;

    /**
     * Represents an order event in the system.
     * This event contains information about the order and its certificate generation status.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class OrderEvent {

        @JsonProperty("order_id")
        private Integer orderId;

        @JsonProperty("product_id")
        private Integer productId;

        @JsonProperty("product_name")
        private String productName;

        private String email;

        @JsonProperty("certificate_key")
        private String certificateKey;

        private Boolean success;

        @JsonProperty("generated_at")
        private LocalDateTime generatedAt;
    }