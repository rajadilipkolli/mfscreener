package com.learning.mfscreener.web.controllers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.learning.mfscreener.common.AbstractIntegrationTest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

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
    void shouldLoadDataWhenSchemeNotFoundAndLoadHistoricalData() throws Exception {

        loadTestData();
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}/{date}", 119578L, "2018-12-20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.schemeCode", is(119578L), Long.class))
                .andExpect(jsonPath("$.payout", is("INF903J01MV8")))
                .andExpect(jsonPath("$.schemeName", is("Sundaram Select Focus Direct Plan - Growth")))
                .andExpect(jsonPath("$.nav", is("173.7261")))
                .andExpect(jsonPath("$.date", is("2018-12-27")))
                .andExpect(jsonPath("$.schemeType", is("Open Ended Schemes(Equity Scheme - Focused Fund)")));
    }

    private void loadTestData() throws Exception {
        File tempFile = File.createTempFile("file", ".json");
        FileWriter fileWriter = getFileWriter(tempFile);
        fileWriter.close();

        try (FileInputStream fileInputStream = new FileInputStream(tempFile)) {

            // Create a MockMultipartFile object
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", // parameter name expected by the controller
                    "file.json", // original file name
                    MediaType.APPLICATION_JSON_VALUE, // content type
                    fileInputStream);

            // Perform the file upload request
            mockMvc.perform(multipart("/api/portfolio/upload").file(multipartFile))
                    .andExpect(status().isOk());
        } finally {
            tempFile.deleteOnExit();
        }
    }

    private static @NotNull FileWriter getFileWriter(File tempFile) throws IOException {
        FileWriter fileWriter = new FileWriter(tempFile);
        fileWriter.write(
                """
                {"statement_period":{"to":"20-Jun-2023","from":"01-Jan-1990"},"file_type":"CAMS","cas_type":"DETAILED","investor_info":{"email":"junit@email.com","name":"Junit","mobile":"9848022338","address":"address"},"folios":[{"PAN":"ABCDE1234F","KYC":"OK","PANKYC":"OK","folio":"501764137099 / 0","amc":"Sundaram Mutual Fund","schemes":[{"rta_code":"119578","open":"0.0","close_calculated":"0.0","valuation":null,"transactions":[],"scheme":"SUNDARAM SELECT FOCUS FUND - DIRECT GROWTH - ISIN: INF903J01MV8","isin":"INF903J01MV8","amfi":119578,"advisor":null,"type":"EQUITY","rta":"KFINTECH","close":"0.0"}]}]}
                """);
        return fileWriter;
    }

    @Test
    void shouldNotLoadHistoricalDataWhenSchemeNotFound() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}/{date}", 144610L, "2021-07-12")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Scheme NotFound")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Fund with schemeCode 144610 Not Found")))
                .andExpect(jsonPath("$.instance", is("/api/nav/144610/2021-07-12")));
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
