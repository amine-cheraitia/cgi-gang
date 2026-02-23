package com.marketplace.payment.infrastructure.rest;

import com.marketplace.payment.application.usecase.ProcessPaymentWebhookUseCase;
import com.marketplace.sales.infrastructure.rest.dto.OrderResponse;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/stripe/webhooks")
@Tag(name = "Payments", description = "Callbacks Stripe")
@ConditionalOnProperty(name = "payment.provider", havingValue = "stripe")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    private final ProcessPaymentWebhookUseCase processPaymentWebhookUseCase;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    public StripeWebhookController(ProcessPaymentWebhookUseCase processPaymentWebhookUseCase) {
        this.processPaymentWebhookUseCase = processPaymentWebhookUseCase;
    }

    @PostMapping
    @Operation(summary = "Recevoir un webhook Stripe", description = "Verifie la signature Stripe et confirme le paiement.")
    public ResponseEntity<?> receive(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
            throw new BusinessException(ErrorCode.PAYMENT_WEBHOOK_INVALID);
        }

        if (!"payment_intent.succeeded".equals(event.getType())) {
            log.debug("Stripe event ignored: {}", event.getType());
            return ResponseEntity.accepted().build();
        }

        StripeObject stripeObject = event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (!(stripeObject instanceof PaymentIntent intent)) {
            return ResponseEntity.accepted().build();
        }

        String orderId = intent.getMetadata().get("orderId");
        if (orderId == null || orderId.isBlank()) {
            log.warn("Stripe webhook: no orderId in PaymentIntent metadata, id={}", intent.getId());
            return ResponseEntity.accepted().build();
        }

        log.info("Stripe payment_intent.succeeded for orderId={}", orderId);
        return processPaymentWebhookUseCase.execute(orderId, "PAID")
                .<ResponseEntity<?>>map(order -> ResponseEntity.ok(OrderResponse.from(order)))
                .orElseGet(() -> ResponseEntity.accepted().build());
    }
}
