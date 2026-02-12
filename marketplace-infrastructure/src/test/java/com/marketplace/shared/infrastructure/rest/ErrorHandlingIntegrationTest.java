package com.marketplace.shared.infrastructure.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ErrorHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Doit retourner un code d'erreur metier stable si listing inexistant")
    void shouldReturnStableErrorCodeForMissingListing() throws Exception {
        String payload = """
            {
              "listingId":"does-not-exist",
              "buyerId":"buyer-seed-1"
            }
            """;

        mockMvc.perform(post("/api/orders")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("buyer", "buyer123"))
                .contentType("application/json")
                .content(payload))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("LST-001"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Doit retourner un code d'erreur metier stable si event introuvable")
    void shouldReturnStableErrorCodeForMissingEvent() throws Exception {
        mockMvc.perform(get("/api/events/evt_unknown"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("CAT-001"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.timestamp").exists());
    }
}
