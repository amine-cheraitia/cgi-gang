package com.marketplace.listing.application.usecase;

import com.marketplace.listing.domain.model.Listing;
import com.marketplace.listing.domain.model.ListingStatus;
import com.marketplace.listing.domain.repository.ListingRepository;
import com.marketplace.listing.domain.valueobject.ExternalEventId;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import com.marketplace.shared.domain.valueobject.Money;
import com.marketplace.storage.domain.port.ObjectStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateListingAttachmentUploadUrlUseCaseTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ObjectStorage objectStorage;

    @InjectMocks
    private GenerateListingAttachmentUploadUrlUseCase useCase;

    @Test
    void shouldGeneratePresignedUrlWhenSupported() {
        Listing listing = Listing.rehydrate("lst_1", new ExternalEventId("evt_1"), "seller_1", Money.euros(80), ListingStatus.PENDING_CERTIFICATION);
        when(listingRepository.findById("lst_1")).thenReturn(Optional.of(listing));
        when(objectStorage.presignUpload(org.mockito.ArgumentMatchers.contains("listings/lst_1/"), eq("application/pdf"), anyLong()))
            .thenReturn(new ObjectStorage.PresignedUpload("k", URI.create("https://upload.local/k"), "PUT", 900));

        var result = useCase.execute("lst_1", "seller_1", "proof.pdf", "application/pdf");

        assertThat(result.uploadUrl()).isEqualTo("https://upload.local/k");
        assertThat(result.method()).isEqualTo("PUT");
    }

    @Test
    void shouldReturnErrorWhenPresignNotSupported() {
        Listing listing = Listing.rehydrate("lst_1", new ExternalEventId("evt_1"), "seller_1", Money.euros(80), ListingStatus.PENDING_CERTIFICATION);
        when(listingRepository.findById("lst_1")).thenReturn(Optional.of(listing));
        when(objectStorage.presignUpload(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), anyLong()))
            .thenThrow(new UnsupportedOperationException("Presigned upload not supported"));

        assertThatThrownBy(() -> useCase.execute("lst_1", "seller_1", "proof.pdf", "application/pdf"))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.LISTING_ATTACHMENT_PRESIGN_UNAVAILABLE);
    }
}
