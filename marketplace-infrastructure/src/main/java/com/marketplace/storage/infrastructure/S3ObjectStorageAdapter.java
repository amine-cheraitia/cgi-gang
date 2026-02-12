package com.marketplace.storage.infrastructure;

import com.marketplace.storage.domain.port.ObjectStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.net.URI;

@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3ObjectStorageAdapter implements ObjectStorage {
    private final StorageProperties properties;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public S3ObjectStorageAdapter(StorageProperties properties) {
        this.properties = properties;
        Region region = Region.of(properties.getS3().getRegion());
        this.s3Client = S3Client.builder().region(region).build();
        this.s3Presigner = S3Presigner.builder().region(region).build();
    }

    @Override
    public StoredObject store(String key, byte[] content, String contentType) {
        String bucket = properties.getS3().getBucket();
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .build();
        s3Client.putObject(request, RequestBody.fromBytes(content));
        return new StoredObject(key, resolve(key));
    }

    @Override
    public URI resolve(String key) {
        String bucket = properties.getS3().getBucket();
        GetObjectRequest objectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(properties.getS3().getSignedUrlDurationSeconds()))
            .getObjectRequest(objectRequest)
            .build();
        return URI.create(s3Presigner.presignGetObject(presignRequest).url().toString());
    }

    @PreDestroy
    void close() {
        s3Client.close();
        s3Presigner.close();
    }
}
