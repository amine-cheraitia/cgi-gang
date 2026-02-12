package com.marketplace.listing.domain.valueobject;

import com.marketplace.shared.domain.valueobject.Money;

public record PriceRange(Money min, Money max) {
    public PriceRange {
        if (min == null || max == null) {
            throw new IllegalArgumentException("PriceRange values cannot be null");
        }
        if (min.isGreaterThan(max)) {
            throw new IllegalArgumentException("Min price cannot be greater than max price");
        }
    }
}
