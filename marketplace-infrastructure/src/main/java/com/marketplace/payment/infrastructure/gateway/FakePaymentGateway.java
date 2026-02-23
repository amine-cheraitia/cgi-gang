package com.marketplace.payment.infrastructure.gateway;

import com.marketplace.payment.domain.port.PaymentGateway;
import com.marketplace.payment.domain.port.PaymentIntentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "payment.provider", havingValue = "fake", matchIfMissing = true)
public class FakePaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(FakePaymentGateway.class);

    @Override
    public PaymentIntentResult createPaymentIntent(String orderId, long amountCents, String currency) {
        log.info("[FAKE] Creating PaymentIntent for order={} amount={}{}",
                orderId, amountCents, currency.toUpperCase());
        String fakeIntentId = "pi_fake_" + orderId;
        String fakeClientSecret = "pi_fake_" + orderId + "_secret_fake";
        return new PaymentIntentResult(fakeIntentId, fakeClientSecret);
    }
}
