package com.marketplace.payment.application.usecase;

import com.marketplace.sales.application.usecase.GetOrderUseCase;
import com.marketplace.sales.application.usecase.MarkOrderPaidUseCase;
import com.marketplace.sales.domain.model.Order;
import com.marketplace.sales.domain.model.OrderStatus;
import com.marketplace.sales.domain.valueobject.PricingBreakdown;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import com.marketplace.shared.domain.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessPaymentWebhookUseCaseTest {

    @Mock
    private MarkOrderPaidUseCase markOrderPaidUseCase;
    @Mock
    private GetOrderUseCase getOrderUseCase;
    @InjectMocks
    private ProcessPaymentWebhookUseCase useCase;

    @Test
    void shouldIgnoreNonPaidStatuses() {
        assertThat(useCase.execute("ord-1", "FAILED")).isEmpty();
        assertThat(useCase.execute("ord-1", null)).isEmpty();
    }

    @Test
    void shouldMarkOrderPaidForPaidStatus() {
        Order order = Order.place("listing-1", "buyer-1", "seller-1", Money.euros(100));
        when(markOrderPaidUseCase.execute("ord-1")).thenReturn(order);

        Optional<Order> result = useCase.execute("ord-1", " paid ");

        assertThat(result).isPresent();
    }

    @Test
    void shouldBeIdempotentWhenAlreadyPaid() {
        Order paid = Order.rehydrate(
            "ord-1",
            "listing-1",
            "buyer-1",
            "seller-1",
            PricingBreakdown.calculate(Money.euros(100)),
            OrderStatus.PAID
        );
        when(markOrderPaidUseCase.execute("ord-1"))
            .thenThrow(new BusinessException(ErrorCode.ORDER_ALREADY_PAID));
        when(getOrderUseCase.execute("ord-1")).thenReturn(paid);

        Optional<Order> result = useCase.execute("ord-1", "PAID");

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void shouldPropagateOtherBusinessErrors() {
        when(markOrderPaidUseCase.execute("ord-1"))
            .thenThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        assertThatThrownBy(() -> useCase.execute("ord-1", "PAID"))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }
}
