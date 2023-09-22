package com.learning.mfscreener.web.controllers;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.learning.mfscreener.common.AbstractIntegrationTest;
import com.learning.mfscreener.utils.TestData;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.time.LocalDate;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PortfolioControllerIT extends AbstractIntegrationTest {

    @Test
    @Order(1)
    void testUploadFile() throws Exception {

        File tempFile = File.createTempFile("file", ".json");
        FileWriter fileWriter = new FileWriter(tempFile);
        fileWriter.write(objectMapper.writeValueAsString(TestData.getCasDTO()));
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
                    .andExpect(status().isOk())
                    .andExpect(content().string("Uploaded with id 1"));
        } finally {
            tempFile.deleteOnExit();
        }
    }

    @Test
    @Order(2)
    void getPortfolio() throws Exception {
        this.mockMvc
                .perform(get("/api/portfolio/{pan}", "ABCDE1234F")
                        .param("date", LocalDate.now().minusDays(2).toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", is("application/json")))
                .andExpect(jsonPath("$.portfolioDetailsDTOS.size()", is(1)));
    }

    @Test
    @Order(3)
    void getPortfolioForAfterDate() throws Exception {
        this.mockMvc
                .perform(get("/api/portfolio/{pan}", "ABCDE1234F")
                        .param("date", LocalDate.now().plusDays(10).toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("getPortfolio.date: Date should be past or today")))
                .andExpect(jsonPath("$.instance", is("/api/portfolio/ABCDE1234F")));
    }
}
