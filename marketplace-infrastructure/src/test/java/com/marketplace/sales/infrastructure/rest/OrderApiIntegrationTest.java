package com.marketplace.sales.infrastructure.rest;

import com.marketplace.testutil.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static com.marketplace.testutil.ApiTestAssertions.assertErrorCode;
import static com.marketplace.testutil.MarketplaceTestDataFactory.orderPayload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderApiIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("POST /api/orders cree une commande avec pricing complet")
    void shouldCreateOrderWithPricing() throws Exception {
        String payload = orderPayload("lst_seed_001", "buyer-seed-1");

        mockMvc.perform(post("/api/orders")
                .with(buyerAuth())
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
        String payload = orderPayload("lst_seed_002", "buyer-seed-1");
        assertErrorCode(mockMvc.perform(post("/api/orders")
                .with(buyerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)),
            409,
            "LST-002");
    }

    @Test
    @DisplayName("POST /api/orders exige role BUYER")
    void shouldRequireBuyerRole() throws Exception {
        String payload = orderPayload("lst_seed_001", "buyer-seed-1");
        assertErrorCode(mockMvc.perform(post("/api/orders")
                .with(sellerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)),
            403,
            "AUTH-003");
    }

    @Test
    @DisplayName("POST /api/orders/{id}/pay confirme le paiement")
    void shouldMarkOrderAsPaid() throws Exception {
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
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    @DisplayName("POST /api/orders/{id}/pay exige role CONTROLLER")
    void shouldRequireControllerRoleToMarkPaid() throws Exception {
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

        assertErrorCode(mockMvc.perform(post("/api/orders/{orderId}/pay", orderId)
                .with(buyerAuth())),
            403,
            "AUTH-003");
    }

    @Test
    @DisplayName("POST /api/orders/{id}/pay refuse un paiement deja confirme")
    void shouldRejectAlreadyPaidOrder() throws Exception {
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

        assertErrorCode(mockMvc.perform(post("/api/orders/{orderId}/pay", orderId)
                .with(controllerAuth()))
            ,
            409,
            "ORD-002");
    }
}
