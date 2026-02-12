package com.marketplace.listing.infrastructure.rest.dto;

import com.marketplace.listing.application.usecase.GenerateListingAttachmentUploadUrlUseCase;

public record AttachmentUploadUrlResponse(
    String key,
    String uploadUrl,
    String method,
    long expiresInSeconds
) {
    public static AttachmentUploadUrlResponse from(GenerateListingAttachmentUploadUrlUseCase.UploadUrl uploadUrl) {
        return new AttachmentUploadUrlResponse(
            uploadUrl.key(),
            uploadUrl.uploadUrl(),
            uploadUrl.method(),
            uploadUrl.expiresInSeconds()
        );
    }
}
