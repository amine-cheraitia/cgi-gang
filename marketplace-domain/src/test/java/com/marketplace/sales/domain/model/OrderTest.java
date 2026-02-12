package com.marketplace.sales.domain.model;

import com.marketplace.sales.domain.valueobject.PricingBreakdown;
import com.marketplace.shared.domain.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void placeShouldCreatePendingOrderWithPricing() {
        Order order = Order.place("listing-1", "buyer-1", "seller-1", Money.euros(100));

        assertThat(order.getId()).isNotBlank();
        assertThat(order.getListingId()).isEqualTo("listing-1");
        assertThat(order.getBuyerId()).isEqualTo("buyer-1");
        assertThat(order.getSellerId()).isEqualTo("seller-1");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(order.getBuyerTotal().amount()).isEqualByComparingTo(BigDecimal.valueOf(112.5));
        assertThat(order.getSellerPayout().amount()).isEqualByComparingTo(BigDecimal.valueOf(95));
        assertThat(order.getPlatformRevenue().amount()).isEqualByComparingTo(BigDecimal.valueOf(17.5));
    }

    @Test
    void placeShouldRejectMissingIdentifiers() {
        assertThatThrownBy(() -> Order.place("", "buyer-1", "seller-1", Money.euros(100)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Listing id is required");
        assertThatThrownBy(() -> Order.place("listing-1", " ", "seller-1", Money.euros(100)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Buyer id is required");
        assertThatThrownBy(() -> Order.place("listing-1", "buyer-1", null, Money.euros(100)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Seller id is required");
    }

    @Test
    void confirmPaymentShouldMovePendingOrderToPaid() {
        Order order = Order.place("listing-1", "buyer-1", "seller-1", Money.euros(100));

        order.confirmPayment();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void confirmPaymentShouldRejectInvalidCurrentStatus() {
        PricingBreakdown pricing = PricingBreakdown.calculate(Money.euros(100));
        Order paidOrder = Order.rehydrate("ord-1", "listing-1", "buyer-1", "seller-1", pricing, OrderStatus.PAID);
        Order failedOrder = Order.rehydrate("ord-2", "listing-1", "buyer-1", "seller-1", pricing, OrderStatus.FAILED);

        assertThatThrownBy(paidOrder::confirmPayment)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Order already paid");
        assertThatThrownBy(failedOrder::confirmPayment)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Order cannot be paid in current state");
    }
}
