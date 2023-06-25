package com.learning.mfscreener.web.controllers;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.learning.mfscreener.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class NavControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldThrowExceptionWhenSchemeNotFound() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}", 1))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Scheme NotFound")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Scheme 1 Not Found")))
                .andExpect(jsonPath("$.instance", is("/api/nav/1")));
    }

    @Test
    void shouldLoadDataWhenSchemeFound() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}", 120503))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", is("application/json")))
                .andExpect(jsonPath("$.schemeCode", is(120503L), Long.class))
                .andExpect(jsonPath("$.payout", is("INF846K01EW2")))
                .andExpect(jsonPath("$.schemeName", is("Axis Long Term Equity Fund - Direct Plan - Growth Option")));
    }
}
