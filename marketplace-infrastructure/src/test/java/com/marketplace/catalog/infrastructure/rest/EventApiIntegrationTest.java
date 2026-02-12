package com.marketplace.catalog.infrastructure.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
}
