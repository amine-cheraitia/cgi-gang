package com.marketplace.listing.infrastructure.rest.dto;

import com.marketplace.listing.application.usecase.GenerateListingAttachmentUploadUrlUseCase;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class AttachmentUploadUrlResponseTest {

    @Test
    void shouldMapFromUseCaseDto() {
        GenerateListingAttachmentUploadUrlUseCase.UploadUrl uploadUrl =
            new GenerateListingAttachmentUploadUrlUseCase.UploadUrl(
                "proof.pdf",
                URI.create("https://example.com/put").toString(),
                "PUT",
                600
            );

        AttachmentUploadUrlResponse response = AttachmentUploadUrlResponse.from(uploadUrl);

        assertThat(response.key()).isEqualTo("proof.pdf");
        assertThat(response.uploadUrl()).isEqualTo("https://example.com/put");
        assertThat(response.method()).isEqualTo("PUT");
        assertThat(response.expiresInSeconds()).isEqualTo(600);
    }
}
