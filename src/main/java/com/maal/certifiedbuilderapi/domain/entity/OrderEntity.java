package com.maal.certifiedbuilderapi.domain.entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "orders")
public class OrderEntity {

    @Id
    private String id;

    private Integer orderId;
    private Integer productId;
    private String participantId;
    private String orderDate;
}