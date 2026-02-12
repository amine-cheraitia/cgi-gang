package com.marketplace.storage.infrastructure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {
    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
    S3Client s3Client(StorageProperties properties) {
        validateS3(properties);
        return S3Client.builder()
            .region(Region.of(properties.getS3().getRegion()))
            .build();
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
    S3Presigner s3Presigner(StorageProperties properties) {
        validateS3(properties);
        return S3Presigner.builder()
            .region(Region.of(properties.getS3().getRegion()))
            .build();
    }

    private void validateS3(StorageProperties properties) {
        if (!StringUtils.hasText(properties.getS3().getBucket())) {
            throw new IllegalStateException("storage.s3.bucket is required when storage.provider=s3");
        }
        if (!StringUtils.hasText(properties.getS3().getRegion())) {
            throw new IllegalStateException("storage.s3.region is required when storage.provider=s3");
        }
        if (properties.getS3().getSignedUrlDurationSeconds() <= 0) {
            throw new IllegalStateException("storage.s3.signed-url-duration-seconds must be > 0");
        }
    }
}
