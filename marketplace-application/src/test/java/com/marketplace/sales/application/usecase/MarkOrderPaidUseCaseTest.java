package com.marketplace.sales.application.usecase;

import com.marketplace.notification.application.event.OrderPaidApplicationEvent;
import com.marketplace.sales.domain.model.Order;
import com.marketplace.sales.domain.model.OrderStatus;
import com.marketplace.sales.domain.repository.OrderRepository;
import com.marketplace.shared.application.event.ApplicationEventDispatcher;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.marketplace.shared.domain.valueobject.Money.euros;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkOrderPaidUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventDispatcher eventDispatcher;

    @InjectMocks
    private MarkOrderPaidUseCase useCase;

    @Test
    void shouldMarkPendingOrderAsPaidAndDispatchEvent() {
        Order order = Order.place("lst_1", "buyer_1", "seller_1", euros(100));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order saved = useCase.execute(order.getId());

        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PAID);
        ArgumentCaptor<OrderPaidApplicationEvent> captor = ArgumentCaptor.forClass(OrderPaidApplicationEvent.class);
        verify(eventDispatcher).dispatch(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(order.getId());
    }

    @Test
    void shouldFailWhenOrderAlreadyPaid() {
        Order paidOrder = Order.rehydrate(
            "ord_1",
            "lst_1",
            "buyer_1",
            "seller_1",
            Order.place("lst_x", "buyer_x", "seller_x", euros(10)).getPricing(),
            OrderStatus.PAID
        );
        when(orderRepository.findById("ord_1")).thenReturn(Optional.of(paidOrder));

        assertThatThrownBy(() -> useCase.execute("ord_1"))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.ORDER_ALREADY_PAID);
    }

    @Test
    void shouldFailWhenOrderNotFound() {
        when(orderRepository.findById("ord_missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("ord_missing"))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    void shouldFailWhenOrderStateIsNotPayable() {
        Order failedOrder = Order.rehydrate(
            "ord_2",
            "lst_1",
            "buyer_1",
            "seller_1",
            Order.place("lst_y", "buyer_y", "seller_y", euros(15)).getPricing(),
            OrderStatus.FAILED
        );
        when(orderRepository.findById("ord_2")).thenReturn(Optional.of(failedOrder));

        assertThatThrownBy(() -> useCase.execute("ord_2"))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.ORDER_INVALID_STATE);
    }
}
