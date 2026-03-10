package com.marketplace.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String loginAndGetToken(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username":"%s",
                      "password":"%s"
                    }
                    """.formatted(username, password)))
            .andReturn()
            .getResponse()
            .getContentAsString();
        return extractStringField(body, "token");
    }

    protected RequestPostProcessor bearer(String token) {
        return request -> {
            request.addHeader("Authorization", "Bearer " + token);
            return request;
        };
    }

    protected String extractStringField(String jsonBody, String fieldName) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(jsonBody);
        JsonNode node = root.get(fieldName);
        if (node == null || node.isNull()) {
            throw new IllegalArgumentException("Champ JSON introuvable: " + fieldName);
        }
        return node.asText();
    }
}
