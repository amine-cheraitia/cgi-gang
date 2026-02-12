package com.marketplace.storage.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ObjectStorageAdapterTest {

    @Mock
    private S3Client s3Client;
    @Mock
    private S3Presigner s3Presigner;
    @Mock
    private PresignedGetObjectRequest presignedGetObjectRequest;
    @Mock
    private PresignedPutObjectRequest presignedPutObjectRequest;

    private S3ObjectStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties();
        properties.getS3().setBucket("bucket-test");
        properties.getS3().setRegion("eu-west-3");
        properties.getS3().setSignedUrlDurationSeconds(600);

        adapter = new S3ObjectStorageAdapter(properties, s3Client, s3Presigner);
    }

    @Test
    void resolveShouldReturnPresignedGetUrl() throws Exception {
        when(s3Presigner.presignGetObject(org.mockito.ArgumentMatchers.any(GetObjectPresignRequest.class)))
            .thenReturn(presignedGetObjectRequest);
        when(presignedGetObjectRequest.url()).thenReturn(java.net.URI.create("https://example.com/get").toURL());

        String resolved = adapter.resolve("proof.pdf").toString();

        assertThat(resolved).isEqualTo("https://example.com/get");
        ArgumentCaptor<GetObjectPresignRequest> captor = ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        verify(s3Presigner).presignGetObject(captor.capture());
        GetObjectRequest objectRequest = captor.getValue().getObjectRequest();
        assertThat(objectRequest.bucket()).isEqualTo("bucket-test");
        assertThat(objectRequest.key()).isEqualTo("proof.pdf");
    }

    @Test
    void storeShouldUploadAndReturnStoredObjectWithResolvedUrl() throws Exception {
        when(s3Presigner.presignGetObject(org.mockito.ArgumentMatchers.any(GetObjectPresignRequest.class)))
            .thenReturn(presignedGetObjectRequest);
        when(presignedGetObjectRequest.url()).thenReturn(java.net.URI.create("https://example.com/get").toURL());

        var stored = adapter.store("proof.pdf", "abc".getBytes(), "application/pdf");

        assertThat(stored.key()).isEqualTo("proof.pdf");
        assertThat(stored.uri().toString()).isEqualTo("https://example.com/get");
        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());
        assertThat(requestCaptor.getValue().bucket()).isEqualTo("bucket-test");
        assertThat(requestCaptor.getValue().key()).isEqualTo("proof.pdf");
        assertThat(requestCaptor.getValue().contentType()).isEqualTo("application/pdf");
    }

    @Test
    void presignUploadShouldReturnPutUrlContract() throws Exception {
        when(s3Presigner.presignPutObject(org.mockito.ArgumentMatchers.any(PutObjectPresignRequest.class)))
            .thenReturn(presignedPutObjectRequest);
        when(presignedPutObjectRequest.url()).thenReturn(java.net.URI.create("https://example.com/put").toURL());

        var upload = adapter.presignUpload("proof.pdf", "application/pdf", 120);

        assertThat(upload.key()).isEqualTo("proof.pdf");
        assertThat(upload.method()).isEqualTo("PUT");
        assertThat(upload.expiresInSeconds()).isEqualTo(120);
        assertThat(upload.uploadUrl().toString()).isEqualTo("https://example.com/put");
    }
}
