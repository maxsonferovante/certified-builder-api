package com.maal.certifiedbuilderapi.domain.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document("certificates")
public class CertificateEntity {

    @Id
    private String id;
    private Boolean success;
    private String certificateKey;

    @Field("order")
    private OrderEntity order;

}
