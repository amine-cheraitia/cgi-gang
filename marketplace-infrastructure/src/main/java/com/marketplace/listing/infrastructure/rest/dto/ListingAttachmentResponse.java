package com.marketplace.listing.infrastructure.rest.dto;

import com.marketplace.listing.application.usecase.UploadListingAttachmentUseCase;

public record ListingAttachmentResponse(String key, String url) {
    public static ListingAttachmentResponse from(UploadListingAttachmentUseCase.UploadedAttachment attachment) {
        return new ListingAttachmentResponse(attachment.key(), attachment.url());
    }
}
