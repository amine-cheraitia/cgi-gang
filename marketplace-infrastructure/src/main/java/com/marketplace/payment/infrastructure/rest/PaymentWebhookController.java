package com.marketplace.payment.infrastructure.rest;

import com.marketplace.payment.application.usecase.ProcessPaymentWebhookUseCase;
import com.marketplace.payment.infrastructure.rest.dto.PaymentWebhookRequest;
import com.marketplace.sales.infrastructure.rest.dto.OrderResponse;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/webhooks")
public class PaymentWebhookController {
    private final ProcessPaymentWebhookUseCase processPaymentWebhookUseCase;

    @Value("${payment.webhook-token:change-me}")
    private String expectedToken;

    public PaymentWebhookController(ProcessPaymentWebhookUseCase processPaymentWebhookUseCase) {
        this.processPaymentWebhookUseCase = processPaymentWebhookUseCase;
    }

    @PostMapping
    public ResponseEntity<?> receive(@RequestHeader(value = "X-Payment-Webhook-Token", required = false) String token,
                                     @Valid @RequestBody PaymentWebhookRequest request) {
        if (token == null || token.isBlank() || !token.equals(expectedToken)) {
            throw new BusinessException(ErrorCode.PAYMENT_WEBHOOK_INVALID);
        }
        return processPaymentWebhookUseCase.execute(request.orderId(), request.status())
            .<ResponseEntity<?>>map(order -> ResponseEntity.ok(OrderResponse.from(order)))
            .orElseGet(() -> ResponseEntity.accepted().build());
    }
}
