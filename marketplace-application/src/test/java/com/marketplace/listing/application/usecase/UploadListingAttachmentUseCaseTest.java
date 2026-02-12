package com.marketplace.listing.application.usecase;

import com.marketplace.listing.domain.model.Listing;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadListingAttachmentUseCaseTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ObjectStorage objectStorage;

    @InjectMocks
    private UploadListingAttachmentUseCase useCase;

    @Test
    void shouldUploadAttachmentForListingOwner() {
        Listing listing = Listing.rehydrate("lst_1", new ExternalEventId("evt_1"), "seller_1", Money.euros(50), com.marketplace.listing.domain.model.ListingStatus.PENDING_CERTIFICATION);
        when(listingRepository.findById("lst_1")).thenReturn(Optional.of(listing));
        when(objectStorage.store(any(), any(), any())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return new ObjectStorage.StoredObject(key, URI.create("http://localhost/files/" + key));
        });

        UploadListingAttachmentUseCase.UploadedAttachment result = useCase.execute(
            "lst_1", "seller_1", "proof.pdf", "application/pdf", new byte[]{1, 2, 3}
        );

        assertThat(result.key()).contains("listings/lst_1/");
        assertThat(result.url()).contains("http://localhost/files/");
    }

    @Test
    void shouldRejectUploadForDifferentSeller() {
        Listing listing = Listing.rehydrate("lst_1", new ExternalEventId("evt_1"), "seller_1", Money.euros(50), com.marketplace.listing.domain.model.ListingStatus.PENDING_CERTIFICATION);
        when(listingRepository.findById("lst_1")).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> useCase.execute("lst_1", "seller_2", "proof.pdf", "application/pdf", new byte[]{1}))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.LISTING_SELLER_MISMATCH);
    }
}
