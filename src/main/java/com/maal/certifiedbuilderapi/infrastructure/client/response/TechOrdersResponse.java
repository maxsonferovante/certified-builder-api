package com.maal.certifiedbuilderapi.infrastructure.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TechOrdersResponse {

    @JsonProperty("order_id")
    private Integer orderId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("cpf")
    private String cpf;

    @JsonProperty("city")
    private String city;

    @JsonProperty("product_id")
    private Integer productId;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("certificate_details")
    private String certificateDetails;

    @JsonProperty("certificate_logo")
    private String certificateLogo;

    @JsonProperty("certificate_background")
    private String certificateBackground;

    @JsonProperty("order_date")
    private String orderDate;

    @JsonProperty("checkin_latitude")
    private String checkinLatitude;

    @JsonProperty("checkin_longitude")
    private String checkinLongitude;

    @JsonProperty("time_checkin")
    private String timeCheckin;
}
