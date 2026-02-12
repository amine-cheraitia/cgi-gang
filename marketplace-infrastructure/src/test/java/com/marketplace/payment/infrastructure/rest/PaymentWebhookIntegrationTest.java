package com.marketplace.payment.infrastructure.rest;

import com.marketplace.notification.infrastructure.email.FakeEmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "payment.webhook-token=test-webhook-token",
    "spring.datasource.url=jdbc:h2:mem:marketplace_payment_webhook;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
    "spring.liquibase.database-change-log-table=databasechangelog_payment_webhook",
    "spring.liquibase.database-change-log-lock-table=databasechangeloglock_payment_webhook"
})
class PaymentWebhookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FakeEmailSender fakeEmailSender;

    @BeforeEach
    void setUp() {
        fakeEmailSender.clear();
    }

    @Test
    @DisplayName("Webhook PAID valide marque la commande et declenche la notification ORDER_PAID")
    void shouldMarkOrderPaidOnValidWebhook() throws Exception {
        String payloadOrder = """
            {
              "listingId":"lst_seed_001",
              "buyerId":"buyer-seed-1"
            }
            """;

        String orderBody = mockMvc.perform(post("/api/orders")
                .with(httpBasic("buyer", "buyer123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadOrder))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String orderId = orderBody.replaceAll(".*\"orderId\":\"([^\"]+)\".*", "$1");
        String webhookPayload = """
            {
              "orderId":"%s",
              "status":"PAID",
              "providerTransactionId":"tx_123"
            }
            """.formatted(orderId);

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
        String webhookPayload = """
            {
              "orderId":"ord_x",
              "status":"PAID"
            }
            """;

        mockMvc.perform(post("/api/payments/webhooks")
                .header("X-Payment-Webhook-Token", "wrong-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(webhookPayload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("PAY-002"));
    }

    @Test
    @DisplayName("Webhook non PAID est accepte sans side-effect")
    void shouldAcceptNonPaidWebhookWithoutUpdatingOrder() throws Exception {
        String webhookPayload = """
            {
              "orderId":"ord_x",
              "status":"PENDING"
            }
            """;

        mockMvc.perform(post("/api/payments/webhooks")
                .header("X-Payment-Webhook-Token", "test-webhook-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(webhookPayload))
            .andExpect(status().isAccepted());
    }

    @Test
    @DisplayName("Webhook PAID rejoue est idempotent et ne casse pas le flux")
    void shouldBeIdempotentWhenPaidWebhookIsRetried() throws Exception {
        String payloadOrder = """
            {
              "listingId":"lst_seed_001",
              "buyerId":"buyer-seed-1"
            }
            """;

        String orderBody = mockMvc.perform(post("/api/orders")
                .with(httpBasic("buyer", "buyer123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadOrder))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String orderId = orderBody.replaceAll(".*\"orderId\":\"([^\"]+)\".*", "$1");
        String webhookPayload = """
            {
              "orderId":"%s",
              "status":"PAID",
              "providerTransactionId":"tx_456"
            }
            """.formatted(orderId);

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

        mockMvc.perform(post("/api/payments/webhooks")
                .header("X-Payment-Webhook-Token", "test-webhook-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(webhookPayload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("GEN-001"));
    }
}
