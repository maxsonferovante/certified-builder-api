package com.maal.certifiedbuilderapi.infrastructure.aws.s3;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
public class S3Properties {
    @Value("${spring.cloud.aws.s3.bucket.name}")
    private String bucketName;
}
