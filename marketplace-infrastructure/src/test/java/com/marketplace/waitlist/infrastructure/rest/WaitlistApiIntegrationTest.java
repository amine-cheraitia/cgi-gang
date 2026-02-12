package com.marketplace.waitlist.infrastructure.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WaitlistApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/waitlist/subscriptions inscrit un buyer")
    void shouldSubscribeBuyerToWaitlist() throws Exception {
        String payload = """
            {
              "eventId":"evt_waitlist_001",
              "userId":"buyer-seed-1"
            }
            """;

        mockMvc.perform(post("/api/waitlist/subscriptions")
                .with(httpBasic("buyer", "buyer123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.eventId").value("evt_waitlist_001"))
            .andExpect(jsonPath("$.userId").value("buyer-seed-1"));
    }

    @Test
    @DisplayName("POST /api/waitlist/subscriptions refuse un doublon")
    void shouldRejectDuplicateWaitlistSubscription() throws Exception {
        String payload = """
            {
              "eventId":"evt_waitlist_dup",
              "userId":"buyer-seed-1"
            }
            """;

        mockMvc.perform(post("/api/waitlist/subscriptions")
                .with(httpBasic("buyer", "buyer123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/waitlist/subscriptions")
                .with(httpBasic("buyer", "buyer123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("WAI-002"));
    }

    @Test
    @DisplayName("DELETE /api/waitlist/subscriptions desinscrit un buyer")
    void shouldUnsubscribeBuyerFromWaitlist() throws Exception {
        String payload = """
            {
              "eventId":"evt_waitlist_unsub",
              "userId":"buyer-seed-1"
            }
            """;

        mockMvc.perform(post("/api/waitlist/subscriptions")
                .with(httpBasic("buyer", "buyer123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/waitlist/subscriptions")
                .with(httpBasic("buyer", "buyer123"))
                .param("eventId", "evt_waitlist_unsub")
                .param("userId", "buyer-seed-1"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/waitlist/subscriptions retourne WAI-001 si abonnement absent")
    void shouldReturnNotFoundWhenUnsubscribeMissingSubscription() throws Exception {
        mockMvc.perform(delete("/api/waitlist/subscriptions")
                .with(httpBasic("buyer", "buyer123"))
                .param("eventId", "evt_unknown")
                .param("userId", "buyer-seed-1"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("WAI-001"));
    }
}
