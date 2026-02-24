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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:marketplace_observer;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
    "spring.liquibase.database-change-log-table=databasechangelog_observer",
    "spring.liquibase.database-change-log-lock-table=databasechangeloglock_observer"
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
        String payload = listingPayload("evt_observer_cert", "seller-seed-1", 75.00, "EUR");

        String body = mockMvc.perform(post("/api/listings")
                .with(sellerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String listingId = extractStringField(body, "id");

        mockMvc.perform(post("/api/certification/{id}/certify", listingId)
                .with(controllerAuth()))
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
        String payload = orderPayload("lst_seed_001", "buyer-seed-1");

        mockMvc.perform(post("/api/orders")
                .with(buyerAuth())
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
        String payload = orderPayload("lst_seed_001", "buyer-seed-1");

        String orderBody = mockMvc.perform(post("/api/orders")
                .with(buyerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String orderId = extractStringField(orderBody, "orderId");

        mockMvc.perform(post("/api/orders/{orderId}/pay", orderId)
                .with(controllerAuth()))
            .andExpect(status().isOk());

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
        String waitlistRequestPayload = waitlistPayload("evt_waitlist_alert", "buyer-seed-1");

        mockMvc.perform(post("/api/waitlist/subscriptions")
                .with(buyerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(waitlistRequestPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.eventId").value("evt_waitlist_alert"));

        String listingRequestPayload = listingPayload("evt_waitlist_alert", "seller-seed-1", 65.00, "EUR");

        String body = mockMvc.perform(post("/api/listings")
                .with(sellerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(listingRequestPayload))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String listingId = extractStringField(body, "id");

        mockMvc.perform(post("/api/certification/{id}/certify", listingId)
                .with(controllerAuth()))
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
        String waitlistRequestPayload = waitlistPayload("evt_psg_om", "buyer-seed-1");

        mockMvc.perform(post("/api/waitlist/subscriptions")
                .with(buyerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(waitlistRequestPayload))
            .andExpect(status().isCreated());

        String listingRequestPayload = listingPayload("evt_psg_om", "seller-seed-1", 66.00, "EUR");

        String body = mockMvc.perform(post("/api/listings")
                .with(sellerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(listingRequestPayload))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String listingId = extractStringField(body, "id");

        mockMvc.perform(post("/api/certification/{id}/certify", listingId)
                .with(controllerAuth()))
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
