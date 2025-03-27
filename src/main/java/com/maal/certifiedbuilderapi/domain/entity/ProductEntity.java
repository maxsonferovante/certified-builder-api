package com.maal.certifiedbuilderapi.domain.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "products")
public class ProductEntity {

    @Id
    private String id;

    private String productName;
    private String certificateDetails;
    private String certificateLogo;
    private String certificateBackground;
    private String checkinLatitude;
    private String checkinLongitude;
    private String timeCheckin;
}
