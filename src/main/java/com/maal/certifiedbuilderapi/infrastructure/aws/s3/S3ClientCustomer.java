package com.maal.certifiedbuilderapi.infrastructure.aws.s3;

import com.maal.certifiedbuilderapi.infrastructure.aws.sqs.OrderEventListener;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import io.awspring.cloud.s3.S3Template;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class S3ClientCustomer {
    private static final Logger logger = LoggerFactory.getLogger(S3ClientCustomer.class);
    private final S3Properties s3Properties;
    private final S3Template s3Template;
    private final S3Client s3Client;

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

    public void deleteProductCertificatesDirectory(Integer productId) {
        try {
            String prefix = "certificates/" + productId + "/";
            logger.info("Deleting directory: {}", prefix);
            
            List<ObjectIdentifier> objectsToDelete = new ArrayList<>();
            String continuationToken = null;
            
            do {
                ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(s3Properties.getBucketName())
                    .prefix(prefix)
                    .continuationToken(continuationToken)
                    .build();
                
                ListObjectsV2Response result = s3Client.listObjectsV2(request);
                
                if (result.contents() != null) {
                    result.contents().forEach(object -> {
                        objectsToDelete.add(ObjectIdentifier.builder()
                            .key(object.key())
                            .build());
                    });
                }
                
                continuationToken = result.nextContinuationToken();
            } while (continuationToken != null);
            
            if (!objectsToDelete.isEmpty()) {
                // Delete objects in batches of 1000 (S3 limit)
                for (int i = 0; i < objectsToDelete.size(); i += 1000) {
                    int endIndex = Math.min(i + 1000, objectsToDelete.size());
                    List<ObjectIdentifier> batch = objectsToDelete.subList(i, endIndex);
                    
                    DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                        .bucket(s3Properties.getBucketName())
                        .delete(software.amazon.awssdk.services.s3.model.Delete.builder()
                            .objects(batch)
                            .build())
                        .build();
                    
                    s3Client.deleteObjects(deleteRequest);
                }
                
                logger.info("Successfully deleted {} objects from directory {}", 
                    objectsToDelete.size(), prefix);
            } else {
                logger.info("No objects found in directory {}", prefix);
            }
        } catch (Exception e) {
            logger.error("Error deleting certificates directory for product {}: {}", 
                productId, e.getMessage());
        }
    }
}
