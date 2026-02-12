package com.marketplace.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected RequestPostProcessor sellerAuth() {
        return SecurityMockMvcRequestPostProcessors.httpBasic("seller", "seller123");
    }

    protected RequestPostProcessor buyerAuth() {
        return SecurityMockMvcRequestPostProcessors.httpBasic("buyer", "buyer123");
    }

    protected RequestPostProcessor controllerAuth() {
        return SecurityMockMvcRequestPostProcessors.httpBasic("controller", "controller123");
    }

    protected RequestPostProcessor invalidSellerAuth() {
        return SecurityMockMvcRequestPostProcessors.httpBasic("seller", "wrong-password");
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
