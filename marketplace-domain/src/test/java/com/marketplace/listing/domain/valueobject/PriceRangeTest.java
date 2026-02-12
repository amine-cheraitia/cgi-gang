package com.marketplace.listing.domain.valueobject;

import com.marketplace.shared.domain.valueobject.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceRangeTest {

    @Test
    void shouldCreateRangeWhenMinLessOrEqualMax() {
        PriceRange range = new PriceRange(Money.euros(50), Money.euros(100));

        assertThat(range.min().amount()).isEqualByComparingTo("50.00");
        assertThat(range.max().amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldRejectInvalidRanges() {
        assertThatThrownBy(() -> new PriceRange(null, Money.euros(10)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("PriceRange values cannot be null");
        assertThatThrownBy(() -> new PriceRange(Money.euros(100), Money.euros(10)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Min price cannot be greater than max price");
    }
}
