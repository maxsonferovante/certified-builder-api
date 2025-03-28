package com.maal.certifiedbuilderapi.infrastructure.aws.s3;


import com.maal.certifiedbuilderapi.infrastructure.aws.sqs.OrderEventListener;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import io.awspring.cloud.s3.S3Template;

import java.net.URL;
import java.time.Duration;


@Component
@AllArgsConstructor
public class S3ClientCustomer {
    private static final Logger logger = LoggerFactory.getLogger(S3ClientCustomer.class);
    private final S3Properties s3Properties;
    private final S3Template  s3Template;

    public String getUrl(String key){
        try{
            logger.info("Getting URL for key {}", key);
            URL response =  s3Template.createSignedGetURL(
                    s3Properties.getBucketName(),
                    key,
                    Duration.ofDays(7L)
            );
            logger.info("Successfully retrieved URL for key {}", response.toString());
            return response.toString();
        } catch (Exception e){
            logger.error(e.getMessage());
            return null;
        }
    }
    public void deleteCertificate(String key) {
        try {
            s3Template.deleteObject(s3Properties.getBucketName(), key);
            logger.info("Successfully deleted key {}", key);
        }
        catch (Exception e){
            logger.error(e.getMessage());
        }
    }
}
