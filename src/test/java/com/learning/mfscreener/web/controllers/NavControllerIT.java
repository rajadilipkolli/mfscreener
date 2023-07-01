package com.learning.mfscreener.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.learning.mfscreener.common.AbstractIntegrationTest;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.MediaType;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NavControllerIT extends AbstractIntegrationTest {

    @BeforeAll
    void setUp() {
        // ensuring that initial data is set so exceptions don't occur
        await().pollInterval(10, TimeUnit.SECONDS).atMost(3, TimeUnit.MINUTES).untilAsserted(() -> assertThat(
                        this.mfSchemeNavEntityRepository.count())
                .isGreaterThan(12875));
    }

    @Test
    void shouldThrowExceptionWhenSchemeNotFound() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}", 1).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
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
                .andExpect(header().string("Content-Type", is("application/json")))
                .andExpect(jsonPath("$.schemeCode", is(120503L), Long.class))
                .andExpect(jsonPath("$.payout", is("INF846K01EW2")))
                .andExpect(jsonPath("$.schemeName", is("Axis Long Term Equity Fund - Direct Plan - Growth Option")))
                .andExpect(jsonPath("$.nav", notNullValue(String.class)))
                .andExpect(jsonPath("$.date", notNullValue(String.class)));
    }

    @Test
    @Disabled
    void shouldLoadDataWhenSchemeFoundAndLoadHistoricalData() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}/{date}", 120503L, "2022-12-20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", is("application/json")))
                .andExpect(jsonPath("$.schemeCode", is(120503L), Long.class))
                .andExpect(jsonPath("$.payout", is("INF846K01EW2")))
                .andExpect(jsonPath("$.schemeName", is("Axis Long Term Equity Fund - Direct Plan - Growth Option")))
                .andExpect(jsonPath("$.nav", is("73.6085")))
                .andExpect(jsonPath("$.date", is("2022-12-20")));
    }

    @Test
    void shouldNotLoadDataWhenSchemeFoundAndLoadHistoricalDataNotFound() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}/{date}", 151113, "2022-10-20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Scheme NotFound")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Nav Not Found for schemeCode - 151113 on 2022-10-16")))
                .andExpect(jsonPath("$.instance", is("/api/nav/151113/2022-10-20")));
    }
}
