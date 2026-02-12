package com.marketplace.sales.application.usecase;

import com.marketplace.sales.domain.model.Order;
import com.marketplace.sales.domain.repository.OrderRepository;
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
class GetOrderUseCaseTest {

    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private GetOrderUseCase useCase;

    @Test
    void shouldReturnOrderWhenExists() {
        Order order = Order.place("listing-1", "buyer-1", "seller-1", Money.euros(100));
        when(orderRepository.findById("ord-1")).thenReturn(Optional.of(order));

        Order result = useCase.execute("ord-1");

        assertThat(result).isSameAs(order);
    }

    @Test
    void shouldThrowOrd001WhenMissing() {
        when(orderRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("missing"))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }
}
