package com.learning.mfscreener.web.controllers;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.learning.mfscreener.common.AbstractIntegrationTest;
import com.learning.mfscreener.models.portfolio.CasDTO;
import com.learning.mfscreener.models.portfolio.InvestorInfoDTO;
import com.learning.mfscreener.models.portfolio.UserFolioDTO;
import com.learning.mfscreener.utils.TestData;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

class XIRRCalculatorControllerIntTest extends AbstractIntegrationTest {

    @Test
    void getXIRR() throws Exception {
        String pan = "XIRRA1234F";
        CasDTO template = TestData.getCasDTO(false, false, false);
        UserFolioDTO folio = template.folios().getFirst();
        CasDTO statement = new CasDTO(
                template.statementPeriod(),
                template.fileType(),
                template.casType(),
                new InvestorInfoDTO("xirr-test@example.com", "XIRR Test", "9848022338", "address"),
                List.of(new UserFolioDTO(
                        "xirr-test-folio", folio.amc(), pan, folio.kyc(), folio.panKyc(), folio.schemes())));
        MockMultipartFile upload = new MockMultipartFile(
                "file", "xirr.json", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(statement));
        mockMvc.perform(multipart("/api/portfolio/upload").file(upload)).andExpect(status().isOk());

        mockMvc.perform(get("/api/xirr/{pan}", pan)
                        .param("asOfDate", "2024-04-15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].folio", is("xirr-test-folio")))
                .andExpect(jsonPath("$[0].amfiId", is(120684)));
    }

    void getXIRRForAfterDate() throws Exception {
        this.mockMvc
                .perform(get("/api/xirr/{pan}", "ABCDE1234F")
                        .param("asOfDate", LocalDate.now().plusDays(10).toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("getXIRR.asOfDate: Date should be past or today")))
                .andExpect(jsonPath("$.instance", is("/api/xirr/ABCDE1234F")));
    }
}
