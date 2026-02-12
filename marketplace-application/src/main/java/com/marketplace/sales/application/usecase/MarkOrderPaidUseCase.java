package com.marketplace.sales.application.usecase;

import com.marketplace.notification.application.event.OrderPaidApplicationEvent;
import com.marketplace.sales.domain.model.Order;
import com.marketplace.sales.domain.repository.OrderRepository;
import com.marketplace.shared.application.event.ApplicationEventDispatcher;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class MarkOrderPaidUseCase {
    private final OrderRepository orderRepository;
    private final ApplicationEventDispatcher eventDispatcher;

    public MarkOrderPaidUseCase(OrderRepository orderRepository, ApplicationEventDispatcher eventDispatcher) {
        this.orderRepository = orderRepository;
        this.eventDispatcher = eventDispatcher;
    }

    public Order execute(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        try {
            order.confirmPayment();
        } catch (IllegalStateException ex) {
            if ("Order already paid".equals(ex.getMessage())) {
                throw new BusinessException(ErrorCode.ORDER_ALREADY_PAID);
            }
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);
        }

        Order saved = orderRepository.save(order);
        eventDispatcher.dispatch(new OrderPaidApplicationEvent(
            saved.getId(),
            saved.getBuyerId(),
            saved.getSellerId(),
            saved.getSellerPayout().amount().toPlainString() + " " + saved.getSellerPayout().currency().getCurrencyCode(),
            saved.getPlatformRevenue().amount().toPlainString() + " " + saved.getPlatformRevenue().currency().getCurrencyCode()
        ));
        return saved;
    }
}
