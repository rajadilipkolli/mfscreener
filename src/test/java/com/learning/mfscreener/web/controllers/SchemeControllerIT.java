package com.learning.mfscreener.web.controllers;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.learning.mfscreener.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class SchemeControllerIT extends AbstractIntegrationTest {

    @Test
    void fetchSchemes() throws Exception {
        this.mockMvc
                .perform(get("/api/scheme/{schemeName}", "sbi small cap").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.size()", is(4)))
                .andExpect(jsonPath("$[*].schemeId", contains(125494, 125495, 125496, 125497)));
    }

    @Test
    void fetchSchemesByFundName() throws Exception {
        this.mockMvc
                .perform(get("/api/scheme/fund/{fundName}", "Mirae Asset Mutual fund")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.size()", is(203)));
    }
}
