package com.maal.certifiedbuilderapi.infrastructure.aws.s3;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import io.awspring.cloud.s3.S3Template;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Cliente S3 para operações com objetos
 * @Profile("!test") garante que não seja carregado durante testes
 */
@Component
@AllArgsConstructor
@Profile("!test")
public class S3ClientCustomer {
    private static final Logger logger = LoggerFactory.getLogger(S3ClientCustomer.class);
    private final S3Properties s3Properties;
    private final S3Template s3Template;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    /**
     * Gera URL pré-assinada para visualização de certificado
     * Configurada com response-content-disposition=inline para exibir no navegador
     * Tempo de expiração: 30 minutos
     * @param key Chave do objeto no S3
     * @return URL pré-assinada ou null em caso de erro
     */
    public String getUrl(String key){
        try{
            logger.info("Getting URL for key {}", key);
            
            // Cria request com response-content-disposition=inline
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .responseContentDisposition("inline")
                    .build();
            
            // Configura presign request com tempo de expiração de 30 minutos
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofDays(7))
                    .getObjectRequest(getObjectRequest)
                    .build();
            
            // Gera URL pré-assinada
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String url = presignedRequest.url().toString();
            
            logger.info("Successfully retrieved URL for key {}", url);
            return url;
        } catch (Exception e){
            logger.error("Error generating presigned URL for key {}: {}", key, e.getMessage());
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
