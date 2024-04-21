package com.learning.mfscreener.web.controllers;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.learning.mfscreener.service.NavService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = NAVController.class)
class NavControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NavService navService;

    @Test
    void shouldThrowBadRequestWhenSchemeCodeIsNotInMaxRange() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}/{date}", 170000, "2023-12-31")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath(
                        "$.detail",
                        is("getSchemeNavOnDate.schemeCode: Max value of schemeCode should be less than 160000")))
                .andExpect(jsonPath("$.instance", is("/api/nav/170000/2023-12-31")));
    }

    @Test
    void shouldThrowBadRequestWhenSchemeCodeIsNotInMinRange() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}", 10000).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath(
                        "$.detail", is("getScheme.schemeCode: Min value of schemeCode should be greater than 100000")))
                .andExpect(jsonPath("$.instance", is("/api/nav/10000")));
    }
}
