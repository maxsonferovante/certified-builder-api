package com.maal.certifiedbuilderapi.domain.entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "participants")
public class ParticipantEntity {

    @Id
    private String id;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String cpf;
    private String city;
}
