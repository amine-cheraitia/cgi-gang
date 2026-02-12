package com.marketplace.listing.domain.model;

import com.marketplace.listing.domain.valueobject.ExternalEventId;
import com.marketplace.shared.domain.valueobject.Money;

import java.util.Objects;
import java.util.UUID;

public class Listing {
    private final String id;
    private final ExternalEventId externalEventId;
    private final String sellerId;
    private final Money price;
    private ListingStatus status;

    private Listing(String id, ExternalEventId externalEventId, String sellerId, Money price) {
        this.id = id;
        this.externalEventId = externalEventId;
        this.sellerId = sellerId;
        this.price = price;
        this.status = ListingStatus.PENDING_CERTIFICATION;
    }

    public static Listing rehydrate(String id, ExternalEventId externalEventId, String sellerId, Money price, ListingStatus status) {
        Listing listing = new Listing(id, externalEventId, sellerId, price);
        listing.status = status;
        return listing;
    }

    public static Listing create(ExternalEventId externalEventId, String sellerId, Money price) {
        if (externalEventId == null) {
            throw new IllegalArgumentException("External event id is required");
        }
        if (sellerId == null || sellerId.isBlank()) {
            throw new IllegalArgumentException("Seller id is required");
        }
        if (price == null || price.isNegative()) {
            throw new IllegalArgumentException("Price must be positive or zero");
        }
        return new Listing(UUID.randomUUID().toString(), externalEventId, sellerId, price);
    }

    public void certify() {
        if (status != ListingStatus.PENDING_CERTIFICATION) {
            throw new IllegalStateException("Only pending listing can be certified");
        }
        status = ListingStatus.CERTIFIED;
    }

    public void reject() {
        if (status != ListingStatus.PENDING_CERTIFICATION) {
            throw new IllegalStateException("Only pending listing can be rejected");
        }
        status = ListingStatus.REJECTED;
    }

    public boolean isPubliclyVisible() {
        return status == ListingStatus.CERTIFIED || status == ListingStatus.SOLD;
    }

    public String getId() {
        return id;
    }

    public ExternalEventId getExternalEventId() {
        return externalEventId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public Money getPrice() {
        return price;
    }

    public ListingStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Listing listing)) return false;
        return Objects.equals(id, listing.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
