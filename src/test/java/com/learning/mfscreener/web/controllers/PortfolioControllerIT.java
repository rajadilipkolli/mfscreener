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
import java.io.IOException;
import java.time.LocalDate;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PortfolioControllerIT extends AbstractIntegrationTest {

    @Test
    @Order(1)
    void uploadFile() throws Exception {

        File tempFile = File.createTempFile("file", ".json");
        FileWriter fileWriter = new FileWriter(tempFile);
        fileWriter.write(objectMapper.writeValueAsString(TestData.getCasDTO(false, false, false)));
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
                    .andExpect(content().string("Imported 1 folios and 1 transactions"));
        } finally {
            tempFile.deleteOnExit();
        }
    }

    @Test
    @Order(2)
    void uploadFileWithNoChanges() throws Exception {

        File tempFile = File.createTempFile("file", ".json");
        FileWriter fileWriter = new FileWriter(tempFile);
        fileWriter.write(objectMapper.writeValueAsString(TestData.getCasDTO(false, false, false)));
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
                    .andExpect(content().string("Nothing to Update"));
        } finally {
            tempFile.deleteOnExit();
        }
    }

    @Test
    @Order(3)
    void uploadFileWithNewFolio() throws Exception {

        File tempFile = File.createTempFile("file", ".json");
        FileWriter fileWriter = new FileWriter(tempFile);
        fileWriter.write(objectMapper.writeValueAsString(TestData.getCasDTO(true, false, false)));
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
                    .andExpect(content().string("Imported 1 folios and 1 transactions"));
        } finally {
            tempFile.deleteOnExit();
        }
    }

    @Test
    @Order(4)
    void uploadFileWithNewScheme() throws Exception {

        File tempFile = File.createTempFile("file", ".json");
        FileWriter fileWriter = new FileWriter(tempFile);
        fileWriter.write(objectMapper.writeValueAsString(TestData.getCasDTO(true, true, false)));
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
                    .andExpect(content().string("Imported 0 folios and 1 transactions"));
        } finally {
            tempFile.deleteOnExit();
        }
    }

    @Test
    @Order(5)
    void uploadFileWithNewTransaction() throws Exception {

        File tempFile = File.createTempFile("file", ".json");
        FileWriter fileWriter = new FileWriter(tempFile);
        fileWriter.write(objectMapper.writeValueAsString(TestData.getCasDTO(true, true, true)));
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
                    .andExpect(content().string("Imported 0 folios and 1 transactions"));
        } finally {
            tempFile.deleteOnExit();
        }
    }

    @Test
    @Order(6)
    void uploadFileWithNewFolioAndSchemeAndTransaction() throws Exception {

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
                    .andExpect(content().string("Imported 1 folios and 3 transactions"));
        } finally {
            tempFile.deleteOnExit();
        }
    }

    @Test
    @Order(7)
    void addTransaction() throws Exception {
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
                    .andExpect(status().isOk())
                    .andExpect(content().string("Imported 1 folios and 3 transactions"));
        } finally {
            tempFile.deleteOnExit();
        }
    }

    private static FileWriter getFileWriter(File tempFile) throws IOException, IOException {
        FileWriter fileWriter = new FileWriter(tempFile);
        fileWriter.write(
                """
                    {"statement_period":{"to":"20-Jun-2023","from":"01-Jan-1990"},"file_type":"CAMS","cas_type":"DETAILED","investor_info":{"email":"junit@email.com","name":"Junit","mobile":"9848022338","address":"address"},"folios":[{"PAN":"ABCDE1234F","KYC":"OK","PANKYC":"OK","folio":"501764137099 / 0","amc":"Sundaram Mutual Fund","schemes":[{"rta_code":"119578","open":"0.0","close_calculated":"0.0","valuation":null,"transactions":[],"scheme":"SUNDARAM SELECT FOCUS FUND - DIRECT GROWTH - ISIN: INF903J01MV8","isin":"INF903J01MV8","amfi":119578,"advisor":null,"type":"EQUITY","rta":"KFINTECH","close":"0.0"}]}]}
                    """);
        return fileWriter;
    }

    @Test
    @Order(101)
    void getPortfolio() throws Exception {
        this.mockMvc
                .perform(get("/api/portfolio/{pan}", "ABCDE1234F")
                        .param("asOfDate", LocalDate.now().minusDays(2).toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.portfolioDetailsDTOS.size()", is(4)));
    }

    @Test
    @Order(102)
    void getPortfolioForAfterDate() throws Exception {
        this.mockMvc
                .perform(get("/api/portfolio/{pan}", "ABCDE1234F")
                        .param("asOfDate", LocalDate.now().plusDays(10).toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("getPortfolio.asOfDate: Date should be past or today")))
                .andExpect(jsonPath("$.instance", is("/api/portfolio/ABCDE1234F")));
    }

    @Test
    @Order(103)
    void getPortfolioWithOutDate() throws Exception {
        this.mockMvc
                .perform(get("/api/portfolio/{pan}", "ABCDE1234F").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.portfolioDetailsDTOS.size()", is(4)));
    }
}
