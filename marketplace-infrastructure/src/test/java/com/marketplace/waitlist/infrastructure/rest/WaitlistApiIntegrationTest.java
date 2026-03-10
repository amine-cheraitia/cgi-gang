package com.marketplace.waitlist.infrastructure.rest;

import com.marketplace.testutil.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static com.marketplace.testutil.ApiTestAssertions.assertErrorCode;
import static com.marketplace.testutil.MarketplaceTestDataFactory.waitlistPayload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WaitlistApiIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("POST /api/waitlist/subscriptions inscrit un buyer")
    void shouldSubscribeBuyerToWaitlist() throws Exception {
        String payload = waitlistPayload("evt_waitlist_001");

        String buyerToken = loginAndGetToken("buyer", "buyer123");

        mockMvc.perform(post("/api/waitlist/subscriptions")
                .with(bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.eventId").value("evt_waitlist_001"));
    }

    @Test
    @DisplayName("POST /api/waitlist/subscriptions refuse un doublon")
    void shouldRejectDuplicateWaitlistSubscription() throws Exception {
        String payload = waitlistPayload("evt_waitlist_dup");

        String buyerToken = loginAndGetToken("buyer", "buyer123");

        mockMvc.perform(post("/api/waitlist/subscriptions")
                .with(bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated());

        assertErrorCode(mockMvc.perform(post("/api/waitlist/subscriptions")
                .with(bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)),
            409,
            "WAI-002");
    }

    @Test
    @DisplayName("DELETE /api/waitlist/subscriptions desinscrit un buyer")
    void shouldUnsubscribeBuyerFromWaitlist() throws Exception {
        String payload = waitlistPayload("evt_waitlist_unsub");

        String buyerToken = loginAndGetToken("buyer", "buyer123");

        mockMvc.perform(post("/api/waitlist/subscriptions")
                .with(bearer(buyerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/waitlist/subscriptions")
                .with(bearer(buyerToken))
                .param("eventId", "evt_waitlist_unsub"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/waitlist/subscriptions retourne WAI-001 si abonnement absent")
    void shouldReturnNotFoundWhenUnsubscribeMissingSubscription() throws Exception {
        String buyerToken = loginAndGetToken("buyer", "buyer123");

        assertErrorCode(mockMvc.perform(delete("/api/waitlist/subscriptions")
                .with(bearer(buyerToken))
                .param("eventId", "evt_unknown")),
            404,
            "WAI-001");
    }

    @Test
    @DisplayName("DELETE /api/waitlist/subscriptions valide la presence des params")
    void shouldValidateUnsubscribeRequestParams() throws Exception {
        String buyerToken = loginAndGetToken("buyer", "buyer123");

        assertErrorCode(mockMvc.perform(delete("/api/waitlist/subscriptions")
                .with(bearer(buyerToken))),
            400,
            "GEN-001");
    }
}
