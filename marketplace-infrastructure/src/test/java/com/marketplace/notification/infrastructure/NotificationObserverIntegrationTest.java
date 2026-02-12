package com.marketplace.notification.infrastructure;

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
    "spring.datasource.url=jdbc:h2:mem:marketplace_observer;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
    "spring.liquibase.database-change-log-table=databasechangelog_observer",
    "spring.liquibase.database-change-log-lock-table=databasechangeloglock_observer"
})
class NotificationObserverIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FakeEmailSender fakeEmailSender;

    @BeforeEach
    void setUp() {
        fakeEmailSender.clear();
    }

    @Test
    @DisplayName("Observer: certifier un listing declenche le template LISTING_CERTIFIED")
    void certifyListingShouldTriggerListingCertifiedTemplate() throws Exception {
        String payload = """
            {
              "eventId":"evt_observer_cert",
              "sellerId":"seller-seed-1",
              "price":75.00,
              "currency":"EUR"
            }
            """;

        String body = mockMvc.perform(post("/api/listings")
                .with(httpBasic("seller", "seller123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String listingId = body.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(post("/api/certification/{id}/certify", listingId)
                .with(httpBasic("controller", "controller123")))
            .andExpect(status().isOk());

        assertThat(fakeEmailSender.sentEmails())
            .anySatisfy(email -> {
                assertThat(email.to()).isEqualTo("seller@marketplace.local");
                assertThat(email.subject()).isEqualTo("Votre billet est certifie");
                assertThat(email.body()).contains("evt_observer_cert");
            });
    }

    @Test
    @DisplayName("Observer: creer une commande declenche le template ORDER_PLACED")
    void placeOrderShouldTriggerOrderPlacedTemplate() throws Exception {
        String payload = """
            {
              "listingId":"lst_seed_001",
              "buyerId":"buyer-seed-1"
            }
            """;

        mockMvc.perform(post("/api/orders")
                .with(httpBasic("buyer", "buyer123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated());

        assertThat(fakeEmailSender.sentEmails())
            .anySatisfy(email -> {
                assertThat(email.to()).isEqualTo("buyer@marketplace.local");
                assertThat(email.subject()).isEqualTo("Commande creee");
                assertThat(email.body()).contains("Montant total a payer");
            });
    }

    @Test
    @DisplayName("Observer: paiement confirme declenche le template ORDER_PAID")
    void markOrderPaidShouldTriggerOrderPaidTemplate() throws Exception {
        String payload = """
            {
              "listingId":"lst_seed_001",
              "buyerId":"buyer-seed-1"
            }
            """;

        String orderBody = mockMvc.perform(post("/api/orders")
                .with(httpBasic("buyer", "buyer123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String orderId = orderBody.replaceAll(".*\"orderId\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(post("/api/orders/{orderId}/pay", orderId)
                .with(httpBasic("controller", "controller123")))
            .andExpect(status().isOk());

        assertThat(fakeEmailSender.sentEmails())
            .anySatisfy(email -> {
                assertThat(email.to()).isEqualTo("seller@marketplace.local");
                assertThat(email.subject()).isEqualTo("Paiement confirme");
                assertThat(email.body()).contains(orderId);
            });
    }

    @Test
    @DisplayName("Observer: certification listing declenche WAITLIST_TICKETS_AVAILABLE pour les inscrits")
    void certifyListingShouldTriggerWaitlistTemplateForSubscribers() throws Exception {
        String waitlistPayload = """
            {
              "eventId":"evt_waitlist_alert",
              "userId":"buyer-seed-1"
            }
            """;

        mockMvc.perform(post("/api/waitlist/subscriptions")
                .with(httpBasic("buyer", "buyer123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(waitlistPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.eventId").value("evt_waitlist_alert"));

        String listingPayload = """
            {
              "eventId":"evt_waitlist_alert",
              "sellerId":"seller-seed-1",
              "price":65.00,
              "currency":"EUR"
            }
            """;

        String body = mockMvc.perform(post("/api/listings")
                .with(httpBasic("seller", "seller123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(listingPayload))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String listingId = body.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(post("/api/certification/{id}/certify", listingId)
                .with(httpBasic("controller", "controller123")))
            .andExpect(status().isOk());

        assertThat(fakeEmailSender.sentEmails())
            .anySatisfy(email -> {
                assertThat(email.to()).isEqualTo("buyer@marketplace.local");
                assertThat(email.subject()).isEqualTo("Billets disponibles");
                assertThat(email.body()).contains("evt_waitlist_alert");
                assertThat(email.body()).contains("65.00 EUR");
            });
    }

    @Test
    @DisplayName("Observer: waitlist utilise le nom evenement catalogue quand disponible")
    void waitlistNotificationShouldUseCatalogEventNameWhenAvailable() throws Exception {
        String waitlistPayload = """
            {
              "eventId":"evt_psg_om",
              "userId":"buyer-seed-1"
            }
            """;

        mockMvc.perform(post("/api/waitlist/subscriptions")
                .with(httpBasic("buyer", "buyer123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(waitlistPayload))
            .andExpect(status().isCreated());

        String listingPayload = """
            {
              "eventId":"evt_psg_om",
              "sellerId":"seller-seed-1",
              "price":66.00,
              "currency":"EUR"
            }
            """;

        String body = mockMvc.perform(post("/api/listings")
                .with(httpBasic("seller", "seller123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(listingPayload))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String listingId = body.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(post("/api/certification/{id}/certify", listingId)
                .with(httpBasic("controller", "controller123")))
            .andExpect(status().isOk());

        assertThat(fakeEmailSender.sentEmails())
            .anySatisfy(email -> {
                assertThat(email.to()).isEqualTo("buyer@marketplace.local");
                assertThat(email.subject()).isEqualTo("Billets disponibles");
                assertThat(email.body()).contains("PSG vs OM");
                assertThat(email.body()).contains("66.00 EUR");
            });
    }
}
