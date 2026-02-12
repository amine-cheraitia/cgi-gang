package com.marketplace.storage.domain.port;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObjectStorageContractTest {

    private final ObjectStorage storage = new ObjectStorage() {
        @Override
        public StoredObject store(String key, byte[] content, String contentType) {
            return new StoredObject(key, URI.create("file:///tmp/" + key));
        }

        @Override
        public URI resolve(String key) {
            return URI.create("file:///tmp/" + key);
        }
    };

    @Test
    void recordsShouldKeepValues() {
        ObjectStorage.StoredObject stored = storage.store("doc.pdf", new byte[]{1}, "application/pdf");
        ObjectStorage.PresignedUpload upload = new ObjectStorage.PresignedUpload(
            "doc.pdf",
            URI.create("https://s3.example/upload"),
            "PUT",
            900
        );

        assertThat(stored.key()).isEqualTo("doc.pdf");
        assertThat(stored.uri().toString()).contains("doc.pdf");
        assertThat(upload.method()).isEqualTo("PUT");
        assertThat(upload.expiresInSeconds()).isEqualTo(900);
    }

    @Test
    void defaultPresignShouldThrowWhenNotSupported() {
        assertThatThrownBy(() -> storage.presignUpload("doc.pdf", "application/pdf", 60))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("Presigned upload not supported");
    }
}
