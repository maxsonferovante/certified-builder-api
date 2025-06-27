package com.maal.certifiedbuilderapi.infrastructure.aws.s3;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Propriedades S3 para ambientes de produção/desenvolvimento
 * @Profile("!test") garante que não seja carregada durante testes
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
@Profile("!test")
public class S3Properties {
    @Value("${spring.cloud.aws.s3.bucket.name}")
    private String bucketName;
}
