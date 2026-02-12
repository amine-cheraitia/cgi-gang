package com.marketplace.payment.infrastructure.rest;

import com.marketplace.payment.application.usecase.ProcessPaymentWebhookUseCase;
import com.marketplace.payment.infrastructure.rest.dto.PaymentWebhookRequest;
import com.marketplace.sales.infrastructure.rest.dto.OrderResponse;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Payments", description = "Callbacks de paiement provider")
public class PaymentWebhookController {
    private final ProcessPaymentWebhookUseCase processPaymentWebhookUseCase;

    @Value("${payment.webhook-token:change-me}")
    private String expectedToken;

    public PaymentWebhookController(ProcessPaymentWebhookUseCase processPaymentWebhookUseCase) {
        this.processPaymentWebhookUseCase = processPaymentWebhookUseCase;
    }

    @PostMapping
    @Operation(summary = "Recevoir un webhook paiement", description = "Traite les callbacks provider et confirme le paiement pour status=PAID.")
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
