package com.marketplace.sales.domain.valueobject;

import com.marketplace.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PricingBreakdown")
class PricingBreakdownTest {

    @Test
    void shouldCalculateCorrectBreakdownForTwentyEuros() {
        PricingBreakdown pricing = PricingBreakdown.calculate(Money.euros(20));

        assertThat(pricing.sellerFee()).isEqualTo(Money.euros(1.00));
        assertThat(pricing.serviceFee()).isEqualTo(Money.euros(2.00));
        assertThat(pricing.transactionFee()).isEqualTo(Money.euros(0.50));
        assertThat(pricing.buyerTotal()).isEqualTo(Money.euros(22.50));
        assertThat(pricing.sellerPayout()).isEqualTo(Money.euros(19.00));
    }

    @Test
    void shouldHandleZeroPrice() {
        PricingBreakdown pricing = PricingBreakdown.calculate(Money.euros(0));
        assertThat(pricing.buyerTotal()).isEqualTo(Money.euros(0));
        assertThat(pricing.platformRevenue()).isEqualTo(Money.euros(0));
    }

    @Test
    void shouldRejectNegativePrice() {
        assertThatThrownBy(() -> PricingBreakdown.calculate(Money.euros(-1)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be negative");
    }

    @Test
    void moneyShouldBeConserved() {
        PricingBreakdown pricing = PricingBreakdown.calculate(Money.euros(80));
        assertThat(pricing.buyerTotal()).isEqualTo(pricing.sellerPayout().add(pricing.platformRevenue()));
    }
}
