package com.marketplace.payment.application.usecase;

import com.marketplace.sales.application.usecase.MarkOrderPaidUseCase;
import com.marketplace.sales.domain.model.Order;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class ProcessPaymentWebhookUseCase {
    private final MarkOrderPaidUseCase markOrderPaidUseCase;

    public ProcessPaymentWebhookUseCase(MarkOrderPaidUseCase markOrderPaidUseCase) {
        this.markOrderPaidUseCase = markOrderPaidUseCase;
    }

    public Optional<Order> execute(String orderId, String paymentStatus) {
        String normalizedStatus = paymentStatus == null ? "" : paymentStatus.trim().toUpperCase(Locale.ROOT);
        if (!"PAID".equals(normalizedStatus)) {
            return Optional.empty();
        }
        return Optional.of(markOrderPaidUseCase.execute(orderId));
    }
}
