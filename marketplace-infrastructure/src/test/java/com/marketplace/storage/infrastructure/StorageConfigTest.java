package com.marketplace.storage.infrastructure;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorageConfigTest {

    private final StorageConfig config = new StorageConfig();

    @Test
    void shouldCreateS3BeansWhenConfigIsValid() {
        StorageProperties props = validProperties();

        try (S3Client s3Client = config.s3Client(props); S3Presigner presigner = config.s3Presigner(props)) {
            assertThat(s3Client).isNotNull();
            assertThat(presigner).isNotNull();
        }
    }

    @Test
    void shouldFailFastWhenS3ConfigIsInvalid() {
        StorageProperties missingBucket = validProperties();
        missingBucket.getS3().setBucket("");
        assertThatThrownBy(() -> config.s3Client(missingBucket))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("storage.s3.bucket is required when storage.provider=s3");

        StorageProperties missingRegion = validProperties();
        missingRegion.getS3().setRegion("");
        assertThatThrownBy(() -> config.s3Presigner(missingRegion))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("storage.s3.region is required when storage.provider=s3");

        StorageProperties invalidDuration = validProperties();
        invalidDuration.getS3().setSignedUrlDurationSeconds(0);
        assertThatThrownBy(() -> config.s3Client(invalidDuration))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("storage.s3.signed-url-duration-seconds must be > 0");
    }

    private StorageProperties validProperties() {
        StorageProperties props = new StorageProperties();
        props.setProvider("s3");
        props.getS3().setBucket("bucket");
        props.getS3().setRegion("eu-west-3");
        props.getS3().setSignedUrlDurationSeconds(300);
        return props;
    }
}
