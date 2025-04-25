package com.maal.certifiedbuilderapi.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class OrderEntity {

    @Id
    private String id;

    //  Documentos Incorporados (Embedded Documents)
    @Field("product")
    private ProductEntity product;  // Produto incorporado no documento

    @Field("participant")
    private ParticipantEntity participant;  // Participante incorporado no documento

    @Indexed(unique = true)
    private Integer orderId;
    private LocalDateTime orderDate;
}
