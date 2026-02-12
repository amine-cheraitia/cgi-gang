package com.marketplace.testutil;

import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class ApiTestAssertions {

    private ApiTestAssertions() {
    }

    public static ResultActions assertErrorCode(ResultActions result, int expectedStatus, String expectedCode) throws Exception {
        return result.andExpect(status().is(expectedStatus))
            .andExpect(jsonPath("$.code").value(expectedCode));
    }
}
