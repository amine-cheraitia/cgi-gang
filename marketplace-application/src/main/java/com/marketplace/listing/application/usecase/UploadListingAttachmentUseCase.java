package com.marketplace.listing.application.usecase;

import com.marketplace.listing.domain.model.Listing;
import com.marketplace.listing.domain.repository.ListingRepository;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import com.marketplace.storage.domain.port.ObjectStorage;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UploadListingAttachmentUseCase {
    private final ListingRepository listingRepository;
    private final ObjectStorage objectStorage;

    public UploadListingAttachmentUseCase(ListingRepository listingRepository, ObjectStorage objectStorage) {
        this.listingRepository = listingRepository;
        this.objectStorage = objectStorage;
    }

    public UploadedAttachment execute(String listingId,
                                      String sellerId,
                                      String originalFilename,
                                      String contentType,
                                      byte[] content) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LISTING_NOT_FOUND));

        if (!listing.getSellerId().equals(sellerId)) {
            throw new BusinessException(ErrorCode.LISTING_SELLER_MISMATCH);
        }
        if (content == null || content.length == 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Attachment file is required");
        }

        String safeName = sanitizeFilename(originalFilename);
        String key = "listings/" + listingId + "/" + UUID.randomUUID() + "-" + safeName;
        String safeContentType = (contentType == null || contentType.isBlank()) ? "application/octet-stream" : contentType;
        ObjectStorage.StoredObject storedObject = objectStorage.store(key, content, safeContentType);
        return new UploadedAttachment(storedObject.key(), storedObject.uri().toString());
    }

    private String sanitizeFilename(String filename) {
        String fallback = "attachment.bin";
        if (filename == null || filename.isBlank()) {
            return fallback;
        }
        String sanitized = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        return sanitized.isBlank() ? fallback : sanitized;
    }

    public record UploadedAttachment(String key, String url) {
    }
}
