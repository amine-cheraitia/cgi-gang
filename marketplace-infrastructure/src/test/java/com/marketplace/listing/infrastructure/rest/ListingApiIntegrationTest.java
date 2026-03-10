package com.marketplace.listing.infrastructure.rest;

import com.marketplace.testutil.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static com.marketplace.testutil.ApiTestAssertions.assertErrorCode;
import static com.marketplace.testutil.MarketplaceTestDataFactory.listingPayload;
import static com.marketplace.testutil.MarketplaceTestDataFactory.presignPayload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ListingApiIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("GET /api/listings expose uniquement les listings certifies")
    void shouldExposeOnlyCertifiedListings() throws Exception {
        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.status == 'PENDING_CERTIFICATION')]").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/listings exige authentification")
    void shouldRequireAuthenticationToCreateListing() throws Exception {
        String payload = listingPayload("evt_new", 70.00, "EUR");

        assertErrorCode(mockMvc.perform(post("/api/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)),
            401,
            "AUTH-001");

        String sellerToken = loginAndGetToken("seller", "seller123");

        mockMvc.perform(post("/api/listings")
                .with(bearer(sellerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING_CERTIFICATION"));

        assertErrorCode(mockMvc.perform(post("/api/listings")
                .with(bearer("invalid-token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)),
            401,
            "AUTH-001");
    }

    @Test
    @DisplayName("POST /api/certification/{id}/certify exige role CONTROLLER")
    void shouldRequireControllerRoleForCertification() throws Exception {
        String payload = listingPayload("evt_cert", 90.00, "EUR");

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

        assertErrorCode(mockMvc.perform(post("/api/certification/{listingId}/certify", listingId)
                .with(bearer(sellerToken))),
            403,
            "AUTH-003");

        mockMvc.perform(post("/api/certification/{listingId}/certify", listingId)
                .with(bearer(controllerToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CERTIFIED"));
    }

    @Test
    @DisplayName("POST /api/listings/{id}/attachments upload une piece pour le vendeur")
    void shouldUploadListingAttachment() throws Exception {
        String payload = listingPayload("evt_attach", 95.00, "EUR");

        String sellerToken = loginAndGetToken("seller", "seller123");

        String body = mockMvc.perform(post("/api/listings")
                .with(bearer(sellerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String listingId = extractStringField(body, "id");
        MockMultipartFile file = new MockMultipartFile("file", "proof.pdf", "application/pdf", "ok".getBytes());

        mockMvc.perform(multipart("/api/listings/{listingId}/attachments", listingId)
                .file(file)
                .with(bearer(sellerToken)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.key").exists())
            .andExpect(jsonPath("$.url").exists());

        String response = mockMvc.perform(multipart("/api/listings/{listingId}/attachments", listingId)
                .file(file)
                .with(bearer(sellerToken)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        String key = extractStringField(response, "key");

        mockMvc.perform(get("/files/{key}", key))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/listings/{id}/attachments refuse un autre seller")
    void shouldRejectListingAttachmentWhenSellerMismatch() throws Exception {
        String payload = listingPayload("evt_attach_forbidden", 90.00, "EUR");

        String sellerToken = loginAndGetToken("seller", "seller123");
        String otherSellerToken = loginAndGetToken("seller2", "seller2123");

        String body = mockMvc.perform(post("/api/listings")
                .with(bearer(sellerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String listingId = extractStringField(body, "id");
        MockMultipartFile file = new MockMultipartFile("file", "proof.pdf", "application/pdf", "ok".getBytes());

        assertErrorCode(mockMvc.perform(multipart("/api/listings/{listingId}/attachments", listingId)
                .file(file)
                .with(bearer(otherSellerToken))),
            403,
            "LST-004");
    }

    @Test
    @DisplayName("POST /api/listings/{id}/attachments/presign retourne LST-005 en provider local")
    void shouldReturnPresignUnavailableOnLocalProvider() throws Exception {
        String payload = listingPayload("evt_presign_local", 88.00, "EUR");

        String sellerToken = loginAndGetToken("seller", "seller123");

        String body = mockMvc.perform(post("/api/listings")
                .with(bearer(sellerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String listingId = extractStringField(body, "id");
        String presignRequestPayload = presignPayload("proof.pdf", "application/pdf");

        assertErrorCode(mockMvc.perform(post("/api/listings/{listingId}/attachments/presign", listingId)
                .with(bearer(sellerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(presignRequestPayload)),
            503,
            "LST-005");
    }

    @Test
    @DisplayName("POST /api/listings/{id}/attachments/presign valide les champs requis")
    void shouldValidatePresignRequestPayload() throws Exception {
        String payload = listingPayload("evt_presign_validation", 88.00, "EUR");

        String sellerToken = loginAndGetToken("seller", "seller123");

        String body = mockMvc.perform(post("/api/listings")
                .with(bearer(sellerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String listingId = extractStringField(body, "id");
        String invalidPayload = """
            {
              "filename":"proof.pdf"
            }
            """;

        assertErrorCode(mockMvc.perform(post("/api/listings/{listingId}/attachments/presign", listingId)
                .with(bearer(sellerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload)),
            400,
            "GEN-001");
    }
}
