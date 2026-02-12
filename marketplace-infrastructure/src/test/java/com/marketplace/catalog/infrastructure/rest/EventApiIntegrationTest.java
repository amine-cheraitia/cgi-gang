package com.marketplace.catalog.infrastructure.rest;

import com.marketplace.testutil.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.marketplace.testutil.ApiTestAssertions.assertErrorCode;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventApiIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("GET /api/events/search retourne les events mockes")
    void shouldReturnMockEvents() throws Exception {
        mockMvc.perform(get("/api/events/search").param("query", "Taylor"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("evt_taylor_paris"))
            .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    @DisplayName("GET /api/events/{id} retourne un event")
    void shouldReturnEventById() throws Exception {
        mockMvc.perform(get("/api/events/evt_psg_om"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("evt_psg_om"))
            .andExpect(jsonPath("$.venue").value("Parc des Princes"));
    }

    @Test
    @DisplayName("GET /api/events/search retourne CAT-002 si provider indisponible")
    void shouldReturnCatalogProviderUnavailableOnSearch() throws Exception {
        assertErrorCode(mockMvc.perform(get("/api/events/search").param("query", "__FAIL__")),
            503,
            "CAT-002");
    }

    @Test
    @DisplayName("GET /api/events/{id} retourne CAT-002 si provider indisponible")
    void shouldReturnCatalogProviderUnavailableOnGetById() throws Exception {
        assertErrorCode(mockMvc.perform(get("/api/events/__FAIL__")),
            503,
            "CAT-002");
    }
}
