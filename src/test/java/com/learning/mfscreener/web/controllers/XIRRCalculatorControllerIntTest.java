package com.learning.mfscreener.web.controllers;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.learning.mfscreener.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class XIRRCalculatorControllerIntTest extends AbstractIntegrationTest {

    @Test
    void getXIRR() throws Exception {
        // Body = [{"folio":"101998485","amfiId":119544,"scheme":"Aditya Birla Sun Life Tax Relief'96 Fund- (ELSS U/S
        // 80C of IT ACT) - Growth-Direct Plan - ISIN: INF209K01UN8","xirr":9.82595386856364},{"folio":"91095687154 /
        // 0","amfiId":120503,"scheme":"Axis ELSS Tax Saver Fund - Direct Growth - ISIN:
        // INF846K01EW2","xirr":12.265001803383841}]
        this.mockMvc
                .perform(get("/api/xirr/{pan}", "ABCDE1234F").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.size()", is(2)));
    }
}
