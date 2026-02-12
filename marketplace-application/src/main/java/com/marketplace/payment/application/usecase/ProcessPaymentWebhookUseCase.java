package com.marketplace.payment.application.usecase;

import com.marketplace.sales.application.usecase.GetOrderUseCase;
import com.marketplace.sales.application.usecase.MarkOrderPaidUseCase;
import com.marketplace.sales.domain.model.Order;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class ProcessPaymentWebhookUseCase {
    private final MarkOrderPaidUseCase markOrderPaidUseCase;
    private final GetOrderUseCase getOrderUseCase;

    public ProcessPaymentWebhookUseCase(MarkOrderPaidUseCase markOrderPaidUseCase,
                                        GetOrderUseCase getOrderUseCase) {
        this.markOrderPaidUseCase = markOrderPaidUseCase;
        this.getOrderUseCase = getOrderUseCase;
    }

    public Optional<Order> execute(String orderId, String paymentStatus) {
        String normalizedStatus = paymentStatus == null ? "" : paymentStatus.trim().toUpperCase(Locale.ROOT);
        if (!"PAID".equals(normalizedStatus)) {
            return Optional.empty();
        }
        try {
            return Optional.of(markOrderPaidUseCase.execute(orderId));
        } catch (BusinessException ex) {
            if (ex.getCode() == ErrorCode.ORDER_ALREADY_PAID) {
                return Optional.of(getOrderUseCase.execute(orderId));
            }
            throw ex;
        }
    }
}
