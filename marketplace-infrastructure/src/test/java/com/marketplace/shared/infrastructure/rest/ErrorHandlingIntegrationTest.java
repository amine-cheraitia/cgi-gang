package com.marketplace.shared.infrastructure.rest;

import com.marketplace.testutil.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.marketplace.testutil.ApiTestAssertions.assertErrorCode;
import static com.marketplace.testutil.MarketplaceTestDataFactory.orderPayload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class ErrorHandlingIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("Doit retourner un code d'erreur metier stable si listing inexistant")
    void shouldReturnStableErrorCodeForMissingListing() throws Exception {
        String payload = orderPayload("does-not-exist", "buyer-seed-1");

        assertErrorCode(mockMvc.perform(post("/api/orders")
                .with(buyerAuth())
                .contentType("application/json")
                .content(payload)),
            404,
            "LST-001")
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Doit retourner un code d'erreur metier stable si event introuvable")
    void shouldReturnStableErrorCodeForMissingEvent() throws Exception {
        assertErrorCode(mockMvc.perform(get("/api/events/evt_unknown")),
            404,
            "CAT-001")
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.timestamp").exists());
    }
}
