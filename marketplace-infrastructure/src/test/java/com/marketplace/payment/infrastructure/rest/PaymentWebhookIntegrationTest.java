package com.marketplace.payment.infrastructure.rest;

import com.marketplace.notification.infrastructure.email.FakeEmailSender;
import com.marketplace.testutil.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static com.marketplace.testutil.ApiTestAssertions.assertErrorCode;
import static com.marketplace.testutil.MarketplaceTestDataFactory.orderPayload;
import static com.marketplace.testutil.MarketplaceTestDataFactory.paymentWebhookPayload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
    "payment.webhook-token=test-webhook-token",
    "spring.datasource.url=jdbc:h2:mem:marketplace_payment_webhook;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
    "spring.liquibase.database-change-log-table=databasechangelog_payment_webhook",
    "spring.liquibase.database-change-log-lock-table=databasechangeloglock_payment_webhook"
})
class PaymentWebhookIntegrationTest extends IntegrationTestBase {

    @Autowired
    private FakeEmailSender fakeEmailSender;

    @BeforeEach
    void setUp() {
        fakeEmailSender.clear();
    }

    @Test
    @DisplayName("Webhook PAID valide marque la commande et declenche la notification ORDER_PAID")
    void shouldMarkOrderPaidOnValidWebhook() throws Exception {
        String payloadOrder = orderPayload("lst_seed_001", "buyer-seed-1");

        String orderBody = mockMvc.perform(post("/api/orders")
                .with(buyerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadOrder))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String orderId = extractStringField(orderBody, "orderId");
        String webhookPayload = paymentWebhookPayload(orderId, "PAID", "tx_123");

        mockMvc.perform(post("/api/payments/webhooks")
                .header("X-Payment-Webhook-Token", "test-webhook-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(webhookPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PAID"));

        assertThat(fakeEmailSender.sentEmails())
            .anySatisfy(email -> {
                assertThat(email.subject()).isEqualTo("Paiement confirme");
                assertThat(email.body()).contains(orderId);
            });
    }

    @Test
    @DisplayName("Webhook avec token invalide retourne PAY-002")
    void shouldRejectWebhookWithInvalidToken() throws Exception {
        String webhookPayload = paymentWebhookPayload("ord_x", "PAID", null);
        assertErrorCode(mockMvc.perform(post("/api/payments/webhooks")
                .header("X-Payment-Webhook-Token", "wrong-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(webhookPayload)),
            400,
            "PAY-002");
    }

    @Test
    @DisplayName("Webhook non PAID est accepte sans side-effect")
    void shouldAcceptNonPaidWebhookWithoutUpdatingOrder() throws Exception {
        String webhookPayload = paymentWebhookPayload("ord_x", "PENDING", null);

        mockMvc.perform(post("/api/payments/webhooks")
                .header("X-Payment-Webhook-Token", "test-webhook-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(webhookPayload))
            .andExpect(status().isAccepted());
    }

    @Test
    @DisplayName("Webhook PAID rejoue est idempotent et ne casse pas le flux")
    void shouldBeIdempotentWhenPaidWebhookIsRetried() throws Exception {
        String payloadOrder = orderPayload("lst_seed_001", "buyer-seed-1");

        String orderBody = mockMvc.perform(post("/api/orders")
                .with(buyerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadOrder))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String orderId = extractStringField(orderBody, "orderId");
        String webhookPayload = paymentWebhookPayload(orderId, "PAID", "tx_456");

        mockMvc.perform(post("/api/payments/webhooks")
                .header("X-Payment-Webhook-Token", "test-webhook-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(webhookPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PAID"));

        int emailsAfterFirstCall = fakeEmailSender.sentEmails().size();

        mockMvc.perform(post("/api/payments/webhooks")
                .header("X-Payment-Webhook-Token", "test-webhook-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(webhookPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PAID"));

        assertThat(fakeEmailSender.sentEmails().size()).isEqualTo(emailsAfterFirstCall);
    }

    @Test
    @DisplayName("Webhook invalide sur payload retourne GEN-001")
    void shouldReturnValidationErrorForInvalidWebhookPayload() throws Exception {
        String webhookPayload = """
            {
              "orderId":"ord_x"
            }
            """;

        assertErrorCode(mockMvc.perform(post("/api/payments/webhooks")
                .header("X-Payment-Webhook-Token", "test-webhook-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(webhookPayload)),
            400,
            "GEN-001");
    }
}
