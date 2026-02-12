package com.marketplace.listing.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record GenerateAttachmentUploadUrlRequest(
    @NotBlank String sellerId,
    @NotBlank String filename,
    @NotBlank String contentType
) {
}
