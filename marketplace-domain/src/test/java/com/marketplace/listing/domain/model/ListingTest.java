package com.marketplace.listing.domain.model;

import com.marketplace.listing.domain.valueobject.ExternalEventId;
import com.marketplace.shared.domain.valueobject.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ListingTest {

    @Test
    void nonCertifiedListingShouldNotBePubliclyVisible() {
        Listing listing = Listing.create(new ExternalEventId("evt-1"), "seller-1", Money.euros(20));
        assertThat(listing.isPubliclyVisible()).isFalse();
    }

    @Test
    void certifiedListingShouldBePubliclyVisible() {
        Listing listing = Listing.create(new ExternalEventId("evt-1"), "seller-1", Money.euros(20));
        listing.certify();
        assertThat(listing.isPubliclyVisible()).isTrue();
    }

    @Test
    void shouldRejectInvalidCreationArguments() {
        assertThatThrownBy(() -> Listing.create(null, "seller-1", Money.euros(20)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("External event id is required");
        assertThatThrownBy(() -> Listing.create(new ExternalEventId("evt-1"), " ", Money.euros(20)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Seller id is required");
        assertThatThrownBy(() -> Listing.create(new ExternalEventId("evt-1"), "seller-1", Money.euros(-1)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Price must be positive or zero");
    }

    @Test
    void shouldRejectAndProtectStateTransitions() {
        Listing listing = Listing.create(new ExternalEventId("evt-1"), "seller-1", Money.euros(20));
        listing.reject();
        assertThat(listing.getStatus()).isEqualTo(ListingStatus.REJECTED);
        assertThat(listing.isPubliclyVisible()).isFalse();

        assertThatThrownBy(listing::certify)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Only pending listing can be certified");
        assertThatThrownBy(listing::reject)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Only pending listing can be rejected");
    }

    @Test
    void soldListingShouldBePubliclyVisibleAndEqualityBasedOnId() {
        Listing sold = Listing.rehydrate(
            "lst-1",
            new ExternalEventId("evt-1"),
            "seller-1",
            Money.euros(20),
            ListingStatus.SOLD
        );
        Listing sameIdDifferentValues = Listing.rehydrate(
            "lst-1",
            new ExternalEventId("evt-2"),
            "seller-2",
            Money.euros(80),
            ListingStatus.PENDING_CERTIFICATION
        );

        assertThat(sold.isPubliclyVisible()).isTrue();
        assertThat(sold).isEqualTo(sameIdDifferentValues);
        assertThat(sold.hashCode()).isEqualTo(sameIdDifferentValues.hashCode());
    }
}
