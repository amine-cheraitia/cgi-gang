package com.marketplace.sales.infrastructure.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/orders cree une commande avec pricing complet")
    void shouldCreateOrderWithPricing() throws Exception {
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
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.pricing.ticketPrice").value(80.00))
            .andExpect(jsonPath("$.pricing.sellerFee").value(4.00))
            .andExpect(jsonPath("$.pricing.serviceFee").value(8.00))
            .andExpect(jsonPath("$.pricing.transactionFee").value(2.00))
            .andExpect(jsonPath("$.pricing.buyerTotal").value(90.00))
            .andExpect(jsonPath("$.pricing.sellerPayout").value(76.00))
            .andExpect(jsonPath("$.pricing.platformRevenue").value(14.00));
    }

    @Test
    @DisplayName("POST /api/orders refuse un listing non certifie")
    void shouldRejectIfListingIsNotCertified() throws Exception {
        String payload = """
            {
              "listingId":"lst_seed_002",
              "buyerId":"buyer-seed-1"
            }
            """;

        mockMvc.perform(post("/api/orders")
                .with(httpBasic("buyer", "buyer123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("LST-002"));
    }

    @Test
    @DisplayName("POST /api/orders exige role BUYER")
    void shouldRequireBuyerRole() throws Exception {
        String payload = """
            {
              "listingId":"lst_seed_001",
              "buyerId":"buyer-seed-1"
            }
            """;

        mockMvc.perform(post("/api/orders")
                .with(httpBasic("seller", "seller123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("AUTH-003"));
    }

    @Test
    @DisplayName("POST /api/orders/{id}/pay confirme le paiement")
    void shouldMarkOrderAsPaid() throws Exception {
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
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    @DisplayName("POST /api/orders/{id}/pay exige role CONTROLLER")
    void shouldRequireControllerRoleToMarkPaid() throws Exception {
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
                .with(httpBasic("buyer", "buyer123")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("AUTH-003"));
    }

    @Test
    @DisplayName("POST /api/orders/{id}/pay refuse un paiement deja confirme")
    void shouldRejectAlreadyPaidOrder() throws Exception {
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

        mockMvc.perform(post("/api/orders/{orderId}/pay", orderId)
                .with(httpBasic("controller", "controller123")))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("ORD-002"));
    }
}
