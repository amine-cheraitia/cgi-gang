package com.marketplace.listing.application.usecase;

import com.marketplace.listing.domain.model.Listing;
import com.marketplace.listing.domain.repository.ListingRepository;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import com.marketplace.storage.domain.port.ObjectStorage;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GenerateListingAttachmentUploadUrlUseCase {
    private static final long DEFAULT_EXPIRATION_SECONDS = 900;

    private final ListingRepository listingRepository;
    private final ObjectStorage objectStorage;

    public GenerateListingAttachmentUploadUrlUseCase(ListingRepository listingRepository, ObjectStorage objectStorage) {
        this.listingRepository = listingRepository;
        this.objectStorage = objectStorage;
    }

    public UploadUrl execute(String listingId, String sellerId, String filename, String contentType) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LISTING_NOT_FOUND));

        if (!listing.getSellerId().equals(sellerId)) {
            throw new BusinessException(ErrorCode.LISTING_SELLER_MISMATCH);
        }

        String safeName = sanitizeFilename(filename);
        String safeContentType = (contentType == null || contentType.isBlank()) ? "application/octet-stream" : contentType;
        String key = "listings/" + listingId + "/" + UUID.randomUUID() + "-" + safeName;

        try {
            ObjectStorage.PresignedUpload presigned = objectStorage.presignUpload(key, safeContentType, DEFAULT_EXPIRATION_SECONDS);
            return new UploadUrl(presigned.key(), presigned.uploadUrl().toString(), presigned.method(), presigned.expiresInSeconds());
        } catch (UnsupportedOperationException ex) {
            throw new BusinessException(ErrorCode.LISTING_ATTACHMENT_PRESIGN_UNAVAILABLE);
        }
    }

    private String sanitizeFilename(String filename) {
        String fallback = "attachment.bin";
        if (filename == null || filename.isBlank()) {
            return fallback;
        }
        String sanitized = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        return sanitized.isBlank() ? fallback : sanitized;
    }

    public record UploadUrl(String key, String uploadUrl, String method, long expiresInSeconds) {
    }
}
