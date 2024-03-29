package com.learning.mfscreener.web.controllers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.learning.mfscreener.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class NavControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldThrowExceptionWhenSchemeNotFound() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}", 1).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Scheme NotFound")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Fund with schemeCode 1 Not Found")))
                .andExpect(jsonPath("$.instance", is("/api/nav/1")));
    }

    @Test
    void shouldLoadDataWhenSchemeFound() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}", 120503L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.schemeCode", is(120503L), Long.class))
                .andExpect(jsonPath("$.payout", is("INF846K01EW2")))
                .andExpect(jsonPath("$.schemeName", is("Axis ELSS Tax Saver Fund - Direct Plan - Growth Option")))
                .andExpect(jsonPath("$.nav", notNullValue(String.class)))
                .andExpect(jsonPath("$.date", notNullValue(String.class)));
    }

    @Test
    void shouldLoadDataWhenSchemeFoundAndLoadHistoricalData() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}/{date}", 120503L, "2022-12-20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.schemeCode", is(120503L), Long.class))
                .andExpect(jsonPath("$.payout", is("INF846K01EW2")))
                .andExpect(jsonPath("$.schemeName", is("Axis ELSS Tax Saver Fund - Direct Plan - Growth Option")))
                .andExpect(jsonPath("$.nav", is("73.6085")))
                .andExpect(jsonPath("$.date", is("2022-12-20")))
                .andExpect(jsonPath("$.schemeType", is("Open Ended Schemes(Equity Scheme - ELSS)")));
    }

    @Test
    void shouldNotLoadDataWhenSchemeFoundAndLoadHistoricalDataNotFound() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}/{date}", 141565, "2017-10-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Scheme NotFound")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Nav Not Found for schemeCode - 141565 on 2017-09-25")))
                .andExpect(jsonPath("$.instance", is("/api/nav/141565/2017-10-01")));
    }

    @Test
    void shouldLoadDataWhenSchemeMergedWithOtherFundHouse() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}/{date}", 151113, "2022-10-20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.schemeCode", is(151113L), Long.class))
                .andExpect(jsonPath("$.payout", is("INF917K01HD4")))
                .andExpect(jsonPath("$.schemeName", is("HSBC Value Fund - Direct Growth")))
                .andExpect(jsonPath("$.nav", is("63.18")))
                .andExpect(jsonPath("$.date", is("2022-10-18")))
                .andExpect(jsonPath("$.schemeType", is("Open Ended Schemes(Equity Scheme - Value Fund)")));
    }
}
