package com.marketplace.listing.infrastructure.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ListingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/listings expose uniquement les listings certifies")
    void shouldExposeOnlyCertifiedListings() throws Exception {
        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.status == 'PENDING_CERTIFICATION')]").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/listings exige authentification SELLER")
    void shouldRequireSellerAuthenticationToCreateListing() throws Exception {
        String payload = """
            {
              "eventId":"evt_new",
              "sellerId":"seller-seed-1",
              "price":70.00,
              "currency":"EUR"
            }
            """;

        mockMvc.perform(post("/api/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("AUTH-001"));

        mockMvc.perform(post("/api/listings")
                .with(httpBasic("seller", "seller123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING_CERTIFICATION"));
    }

    @Test
    @DisplayName("POST /api/certification/{id}/certify exige role CONTROLLER")
    void shouldRequireControllerRoleForCertification() throws Exception {
        String payload = """
            {
              "eventId":"evt_cert",
              "sellerId":"seller-seed-1",
              "price":90.00,
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

        mockMvc.perform(post("/api/certification/{listingId}/certify", listingId)
                .with(httpBasic("seller", "seller123")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("AUTH-003"));

        mockMvc.perform(post("/api/certification/{listingId}/certify", listingId)
                .with(httpBasic("controller", "controller123")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CERTIFIED"));
    }

    @Test
    @DisplayName("POST /api/listings/{id}/attachments upload une piece pour le vendeur")
    void shouldUploadListingAttachment() throws Exception {
        String payload = """
            {
              "eventId":"evt_attach",
              "sellerId":"seller-seed-1",
              "price":95.00,
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
        MockMultipartFile file = new MockMultipartFile("file", "proof.pdf", "application/pdf", "ok".getBytes());

        mockMvc.perform(multipart("/api/listings/{listingId}/attachments", listingId)
                .file(file)
                .param("sellerId", "seller-seed-1")
                .with(httpBasic("seller", "seller123")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.key").exists())
            .andExpect(jsonPath("$.url").exists());

        String response = mockMvc.perform(multipart("/api/listings/{listingId}/attachments", listingId)
                .file(file)
                .param("sellerId", "seller-seed-1")
                .with(httpBasic("seller", "seller123")))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        String key = response.replaceAll(".*\"key\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/files/{key}", key))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/listings/{id}/attachments refuse un autre seller")
    void shouldRejectListingAttachmentWhenSellerMismatch() throws Exception {
        String payload = """
            {
              "eventId":"evt_attach_forbidden",
              "sellerId":"seller-seed-1",
              "price":90.00,
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
        MockMultipartFile file = new MockMultipartFile("file", "proof.pdf", "application/pdf", "ok".getBytes());

        mockMvc.perform(multipart("/api/listings/{listingId}/attachments", listingId)
                .file(file)
                .param("sellerId", "seller-seed-2")
                .with(httpBasic("seller", "seller123")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("LST-004"));
    }

    @Test
    @DisplayName("POST /api/listings/{id}/attachments/presign retourne LST-005 en provider local")
    void shouldReturnPresignUnavailableOnLocalProvider() throws Exception {
        String payload = """
            {
              "eventId":"evt_presign_local",
              "sellerId":"seller-seed-1",
              "price":88.00,
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
        String presignPayload = """
            {
              "sellerId":"seller-seed-1",
              "filename":"proof.pdf",
              "contentType":"application/pdf"
            }
            """;

        mockMvc.perform(post("/api/listings/{listingId}/attachments/presign", listingId)
                .with(httpBasic("seller", "seller123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(presignPayload))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.code").value("LST-005"));
    }
}
