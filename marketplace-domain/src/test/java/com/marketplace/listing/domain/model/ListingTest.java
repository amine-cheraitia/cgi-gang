package com.marketplace.listing.domain.model;

import com.marketplace.listing.domain.valueobject.ExternalEventId;
import com.marketplace.shared.domain.valueobject.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
