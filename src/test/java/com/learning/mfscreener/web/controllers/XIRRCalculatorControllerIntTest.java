package com.learning.mfscreener.web.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.learning.mfscreener.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class XIRRCalculatorControllerIntTest extends AbstractIntegrationTest {

    @Test
    void getXIRR() throws Exception {
        this.mockMvc
                .perform(get("/api/xirr/{pan}", "ABCDE1234F").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
