package com.marketplace.notification.infrastructure;

import com.marketplace.notification.infrastructure.email.FakeEmailSender;
import com.marketplace.testutil.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static com.marketplace.testutil.MarketplaceTestDataFactory.listingPayload;
import static com.marketplace.testutil.MarketplaceTestDataFactory.orderPayload;
import static com.marketplace.testutil.MarketplaceTestDataFactory.waitlistPayload;
import static com.marketplace.testutil.MarketplaceTestDataFactory.paymentWebhookPayload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:marketplace_observer;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
    "spring.liquibase.database-change-log-table=databasechangelog_observer",
    "spring.liquibase.database-change-log-lock-table=databasechangeloglock_observer",
    "payment.webhook-token=test-webhook-token"
})
class NotificationObserverIntegrationTest extends IntegrationTestBase {

    @Autowired
    private FakeEmailSender fakeEmailSender;

    @BeforeEach
    void setUp() {
        fakeEmailSender.clear();
    }

    @Test
    @DisplayName("Observer: certifier un listing declenche le template LISTING_CERTIFIED")
    void certifyListingShouldTriggerListingCertifiedTemplate() throws Exception {
        String payload = listingPayload("evt_observer_cert", 75.00, "EUR");

        String sellerToken = loginAndGetToken("seller", "seller123");
        String controllerToken = loginAndGetToken("controller", "controller123");

        String body = mockMvc.perform(post("/api/listings")
                .with(bearer(sellerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String listingId = extractStringField(body, "id");

        // Upload du ticket PDF requis avant certification
        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile("file", "proof.pdf", "application/pdf", "ok".getBytes());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/listings/{id}/attachments", listingId)
                .file(file)
                .with(bearer(sellerToken)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/certification/{id}/certify", listingId)
                .with(bearer(controllerToken)))
            .andExpect(status().isOk());

        assertThat(fakeEmailSender.sentEmails())
            .anySatisfy(email -> {
                assertThat(email.to()).isEqualTo("seller@marketplace.local");
                assertThat(email.subject()).contains("certifie");
                assertThat(email.body()).contains("evt_observer_cert");
            });
    }

    @Test
    @DisplayName("Observer: creer une commande declenche le template ORDER_PLACED")
    void placeOrderShouldTriggerOrderPlacedTemplate() throws Exception {
        String payload = orderPayload("lst_seed_001");

        String buyerToken = loginAndGetToken("buyer", "buyer123");

        mockMvc.perform(post("/api/orders")
                .with(bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated());

        assertThat(fakeEmailSender.sentEmails())
            .anySatisfy(email -> {
                assertThat(email.to()).isEqualTo("buyer@marketplace.local");
                assertThat(email.subject()).contains("commande");
                assertThat(email.body()).contains("Montant total");
            });
    }

    @Test
    @DisplayName("Observer: paiement confirme declenche le template ORDER_PAID")
    void markOrderPaidShouldTriggerOrderPaidTemplate() throws Exception {
        String payload = orderPayload("lst_seed_001");

        String buyerToken = loginAndGetToken("buyer", "buyer123");

        String orderBody = mockMvc.perform(post("/api/orders")
                .with(bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String orderId = extractStringField(orderBody, "orderId");

        String webhookPayload = paymentWebhookPayload(orderId, "PAID", "tx_observer");

        mockMvc.perform(post("/api/payments/webhooks")
                .header("X-Payment-Webhook-Token", "test-webhook-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(webhookPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PAID"));

        assertThat(fakeEmailSender.sentEmails())
            .anySatisfy(email -> {
                assertThat(email.to()).isEqualTo("seller@marketplace.local");
                assertThat(email.subject()).contains("Paiement confirme");
                assertThat(email.body()).contains(orderId);
            });
    }

    @Test
    @DisplayName("Observer: certification listing declenche WAITLIST_TICKETS_AVAILABLE pour les inscrits")
    void certifyListingShouldTriggerWaitlistTemplateForSubscribers() throws Exception {
        String waitlistRequestPayload = waitlistPayload("evt_waitlist_alert");

        String buyerToken = loginAndGetToken("buyer", "buyer123");
        String sellerToken = loginAndGetToken("seller", "seller123");
        String controllerToken = loginAndGetToken("controller", "controller123");

        mockMvc.perform(post("/api/waitlist/subscriptions")
                .with(bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(waitlistRequestPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.eventId").value("evt_waitlist_alert"));

        String listingRequestPayload = listingPayload("evt_waitlist_alert", 65.00, "EUR");

        String body = mockMvc.perform(post("/api/listings")
                .with(bearer(sellerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(listingRequestPayload))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String listingId = extractStringField(body, "id");

        // Upload du ticket PDF requis avant certification
        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile("file", "proof.pdf", "application/pdf", "ok".getBytes());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/listings/{id}/attachments", listingId)
                .file(file)
                .with(bearer(sellerToken)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/certification/{id}/certify", listingId)
                .with(bearer(controllerToken)))
            .andExpect(status().isOk());

        assertThat(fakeEmailSender.sentEmails())
            .anySatisfy(email -> {
                assertThat(email.to()).isEqualTo("buyer@marketplace.local");
                assertThat(email.subject()).contains("disponibles");
                assertThat(email.body()).contains("evt_waitlist_alert");
                assertThat(email.body()).contains("65.00 EUR");
            });
    }

    @Test
    @DisplayName("Observer: waitlist utilise le nom evenement catalogue quand disponible")
    void waitlistNotificationShouldUseCatalogEventNameWhenAvailable() throws Exception {
        String waitlistRequestPayload = waitlistPayload("evt_psg_om");

        String buyerToken = loginAndGetToken("buyer", "buyer123");
        String sellerToken = loginAndGetToken("seller", "seller123");
        String controllerToken = loginAndGetToken("controller", "controller123");

        mockMvc.perform(post("/api/waitlist/subscriptions")
                .with(bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(waitlistRequestPayload))
            .andExpect(status().isCreated());

        String listingRequestPayload = listingPayload("evt_psg_om", 66.00, "EUR");

        String body = mockMvc.perform(post("/api/listings")
                .with(bearer(sellerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(listingRequestPayload))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String listingId = extractStringField(body, "id");

        // Upload du ticket PDF requis avant certification
        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile("file", "proof.pdf", "application/pdf", "ok".getBytes());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/listings/{id}/attachments", listingId)
                .file(file)
                .with(bearer(sellerToken)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/certification/{id}/certify", listingId)
                .with(bearer(controllerToken)))
            .andExpect(status().isOk());

        assertThat(fakeEmailSender.sentEmails())
            .anySatisfy(email -> {
                assertThat(email.to()).isEqualTo("buyer@marketplace.local");
                assertThat(email.subject()).contains("disponibles");
                assertThat(email.body()).contains("PSG vs OM");
                assertThat(email.body()).contains("66.00 EUR");
            });
    }
}
