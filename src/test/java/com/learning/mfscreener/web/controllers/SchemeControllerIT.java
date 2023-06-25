package com.learning.mfscreener.web.controllers;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.learning.mfscreener.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class SchemeControllerIT extends AbstractIntegrationTest {

    @Test
    void fetchSchemes() throws Exception {
        this.mockMvc
                .perform(get("/api/scheme/{schemeName}", "sbi small cap"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", is("application/json")))
                .andExpect(jsonPath("$.size()", is(4)))
                .andExpect(jsonPath("$[*].schemeId", contains(125494, 125495, 125496, 125497)));
    }

    @Test
    void fetchSchemesByFundName() throws Exception {
        this.mockMvc
                .perform(get("/api/scheme/fund/{fundName}", "Mirae Asset Mutual fund"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", is("application/json")))
                .andExpect(jsonPath("$.size()", is(0)));
    }
}
