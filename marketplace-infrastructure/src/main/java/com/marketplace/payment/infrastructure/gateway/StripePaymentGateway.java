package com.marketplace.payment.infrastructure.gateway;

import com.marketplace.payment.domain.port.PaymentGateway;
import com.marketplace.payment.domain.port.PaymentIntentResult;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "payment.provider", havingValue = "stripe")
public class StripePaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentGateway.class);

    @Value("${stripe.secret-key}")
    private String secretKey;

    @PostConstruct
    void init() {
        Stripe.apiKey = secretKey;
        log.info("Stripe payment gateway initialized");
    }

    @Override
    public PaymentIntentResult createPaymentIntent(String orderId, long amountCents, String currency) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountCents)
                    .setCurrency(currency.toLowerCase())
                    .putMetadata("orderId", orderId)
                    .addPaymentMethodType("card")
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            log.info("Stripe PaymentIntent created: id={} for orderId={}", intent.getId(), orderId);
            return new PaymentIntentResult(intent.getId(), intent.getClientSecret());
        } catch (StripeException e) {
            log.error("Stripe error creating PaymentIntent for orderId={}: {}", orderId, e.getMessage());
            throw new IllegalStateException("Stripe payment provider error: " + e.getMessage(), e);
        }
    }
}
