package com.marketplace.shared.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void shouldSupportArithmeticAndComparisons() {
        Money eur10 = Money.euros(10);
        Money eur2 = Money.euros(2);

        assertThat(eur10.add(eur2).amount()).isEqualByComparingTo("12.00");
        assertThat(eur10.subtract(eur2).amount()).isEqualByComparingTo("8.00");
        assertThat(eur10.multiply(BigDecimal.valueOf(1.5)).amount()).isEqualByComparingTo("15.00");
        assertThat(eur10.divide(4).amount()).isEqualByComparingTo("2.50");
        assertThat(eur10.isGreaterThan(eur2)).isTrue();
        assertThat(eur2.isLessThan(eur10)).isTrue();
    }

    @Test
    void shouldExposeFactoriesAndNegativeState() {
        assertThat(Money.usd(5).currency()).isEqualTo(Currency.getInstance("USD"));
        assertThat(Money.of(BigDecimal.ONE, Currency.getInstance("EUR")).currency())
            .isEqualTo(Currency.getInstance("EUR"));
        assertThat(Money.euros(-1).isNegative()).isTrue();
    }

    @Test
    void shouldRejectInvalidInputs() {
        assertThatThrownBy(() -> new Money(null, Currency.getInstance("EUR")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Amount cannot be null");
        assertThatThrownBy(() -> new Money(BigDecimal.ONE, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Currency cannot be null");
        assertThatThrownBy(() -> Money.euros(10).multiply(BigDecimal.valueOf(-1)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Factor cannot be negative");
        assertThatThrownBy(() -> Money.euros(10).divide(0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Divisor must be positive");
        assertThatThrownBy(() -> Money.euros(10).add(Money.usd(1)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Currency mismatch");
        assertThatThrownBy(() -> Money.euros(10).add(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Money cannot be null");
    }
}
