package com.learning.mfscreener.web.controllers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PortfolioControllerIT extends AbstractIntegrationTest {

    @Test
    @Order(1)
    void uploadFile() throws Exception {

        File tempFile = File.createTempFile("file", ".json");
        try (FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(objectMapper.writeValueAsString(TestData.getCasDTO(false, false, false)));
        }

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
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.importSummary", is("Imported 1 folios and 5 transactions")))
                    .andExpect(jsonPath("$.summaryByFY.FY2022-23", notNullValue()));
        } finally {
            tempFile.deleteOnExit();
        }
    }

    @Test
    @Order(2)
    void uploadFileWithNoChanges() throws Exception {

        File tempFile = File.createTempFile("file", ".json");
        try (FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(objectMapper.writeValueAsString(TestData.getCasDTO(false, false, false)));
        }

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
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.importSummary", is("Nothing to Update")));
        } finally {
            tempFile.deleteOnExit();
        }
    }

    @Test
    @Order(3)
    void uploadFileWithNewFolio() throws Exception {

        File tempFile = File.createTempFile("file", ".json");
        try (FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(objectMapper.writeValueAsString(TestData.getCasDTO(true, false, false)));
        }

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
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.importSummary", is("Imported 1 folios and 1 transactions")));
        } finally {
            tempFile.deleteOnExit();
        }
    }

    @Test
    @Order(4)
    void uploadFileWithNewScheme() throws Exception {

        File tempFile = File.createTempFile("file", ".json");
        try (FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(objectMapper.writeValueAsString(TestData.getCasDTO(true, true, false)));
        }

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
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.importSummary", is("Imported 0 folios and 6 transactions")))
                    .andExpect(jsonPath("$.summaryByFY.FY2022-23", notNullValue()));
        } finally {
            tempFile.deleteOnExit();
        }
    }

    @Test
    @Order(5)
    void uploadFileWithNewTransaction() throws Exception {

        File tempFile = File.createTempFile("file", ".json");
        try (FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(objectMapper.writeValueAsString(TestData.getCasDTO(true, true, true)));
        }
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
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.importSummary", is("Imported 0 folios and 1 transactions")));
        } finally {
            tempFile.deleteOnExit();
        }
    }

    @Test
    @Order(6)
    void uploadFileWithNewFolioAndSchemeAndTransaction() throws Exception {

        File tempFile = File.createTempFile("file", ".json");
        try (FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(objectMapper.writeValueAsString(TestData.getCasDTO()));
        }

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
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.importSummary", is("Imported 1 folios and 3 transactions")));
        } finally {
            tempFile.deleteOnExit();
        }
    }

    @Test
    @Order(7)
    void addMultipleSchemesAndTransactions() throws Exception {
        File tempFile = File.createTempFile("file", ".json");
        try (FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(getUploadJson());
        }

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
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.importSummary", is("Imported 1 folios and 59 transactions")));
        } finally {
            tempFile.deleteOnExit();
        }
    }

    private String getUploadJson() {
        return """
                    {"cas_type": "DETAILED", "file_type": "CAMS", "folios": [{"amc": "Sundaram Mutual Fund", "folio": "501764629146 / 0", "KYC": "OK", "PAN": "ABCDE1234F", "PANKYC": "OK", "schemes": [{"advisor": "INZ000208032", "amfi": "148507", "close": 0.0, "close_calculated": 0.0, "isin": "INF903JA1JC0", "open": 0.0, "rta": "KFINTECH", "rta_code": "176BCDG", "scheme": "SUNDARAM LARGE CAP FUND - DIRECT GROWTH - ISIN: INF903JA1JC0", "transactions": [{"amount": 5181.46, "balance": 348.739, "date": "2021-12-24", "description": "Switch Over In", "dividend_rate": null, "nav": 14.8577, "type": "SWITCH_IN", "units": 348.739}, {"amount": -885.51, "balance": 289.967, "date": "2022-02-15", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 15.067, "type": "REDEMPTION", "units": -58.772}, {"amount": 0.01, "balance": null, "date": "2022-02-15", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -120.16, "balance": 282.087, "date": "2022-04-01", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 15.2498, "type": "REDEMPTION", "units": -7.88}, {"amount": 0.01, "balance": null, "date": "2022-04-01", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -4319.08, "balance": 0.0, "date": "2022-08-23", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 15.3113, "type": "REDEMPTION", "units": -282.087}, {"amount": 0.04, "balance": null, "date": "2022-08-23", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}], "type": "EQUITY", "valuation": {"date": "2024-04-12", "nav": 20.6131, "value": 0}}, {"advisor": "ARN-0845", "amfi": "141565", "close": 0.0, "close_calculated": 0.0, "isin": "INF903JA1AX5", "open": 0.0, "rta": "KFINTECH", "rta_code": "1769OGP", "scheme": "SUNDARAM LONG TERM MICRO CAP TAX ADVANTAGE FUND SERIES VI - 10 YEARS - REGULAR GROWTH - ISIN: INF903JA1AX5", "transactions": [{"amount": 3000.0, "balance": 300.0, "date": "2017-09-28", "description": "Initial Purchase", "dividend_rate": null, "nav": 10.0, "type": "PURCHASE", "units": 300.0}, {"amount": -4074.56, "balance": 0.0, "date": "2021-08-16", "description": "Switch Over Out less TDS, STT", "dividend_rate": null, "nav": 13.582, "type": "SWITCH_OUT", "units": -300.0}, {"amount": 0.04, "balance": null, "date": "2021-08-16", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}], "type": "EQUITY", "valuation": {"date": "2024-04-12", "nav": 23.6892, "value": 0}}, {"advisor": "INZ000208032", "amfi": "119578", "close": 0.0, "close_calculated": 0.0, "isin": "INF903J01MV8", "open": 0.0, "rta": "KFINTECH", "rta_code": "176SFDG", "scheme": "SUNDARAM SELECT FOCUS FUND - DIRECT GROWTH - ISIN: INF903J01MV8", "transactions": [{"amount": 100.0, "balance": 0.528, "date": "2019-10-14", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 189.5308, "type": "PURCHASE_SIP", "units": 0.528}, {"amount": 100.0, "balance": 1.026, "date": "2019-11-20", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 200.7917, "type": "PURCHASE_SIP", "units": 0.498}, {"amount": 100.0, "balance": 1.516, "date": "2019-12-20", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 204.1145, "type": "PURCHASE_SIP", "units": 0.49}, {"amount": 200.0, "balance": 2.491, "date": "2020-01-27", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 205.0402, "type": "PURCHASE_SIP", "units": 0.975}, {"amount": 200.0, "balance": 3.472, "date": "2020-02-25", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 203.889, "type": "PURCHASE_SIP", "units": 0.981}, {"amount": 200.0, "balance": 4.611, "date": "2020-03-13", "description": "Purchase", "dividend_rate": null, "nav": 175.5659, "type": "PURCHASE", "units": 1.139}, {"amount": 200.0, "balance": 6.012, "date": "2020-03-25", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 142.7603, "type": "PURCHASE_SIP", "units": 1.401}, {"amount": 200.0, "balance": 7.231, "date": "2020-04-27", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 164.0448, "type": "PURCHASE_SIP", "units": 1.219}, {"amount": 200.0, "balance": 8.483, "date": "2020-05-26", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 159.7268, "type": "PURCHASE_SIP", "units": 1.252}, {"amount": 200.0, "balance": 9.618, "date": "2020-06-25", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 176.2505, "type": "PURCHASE_SIP", "units": 1.135}, {"amount": 199.99, "balance": 10.691, "date": "2020-07-27", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 186.4209, "type": "PURCHASE_SIP", "units": 1.073}, {"amount": 0.01, "balance": null, "date": "2020-07-27", "description": "*** Stamp Duty ***", "dividend_rate": null, "nav": null, "type": "STAMP_DUTY_TAX", "units": null}, {"amount": 299.99, "balance": 12.239, "date": "2020-08-25", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 193.84, "type": "PURCHASE_SIP", "units": 1.548}, {"amount": 0.01, "balance": null, "date": "2020-08-25", "description": "*** Stamp Duty ***", "dividend_rate": null, "nav": null, "type": "STAMP_DUTY_TAX", "units": null}, {"amount": 699.97, "balance": 16.005, "date": "2020-09-25", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 185.8557, "type": "PURCHASE_SIP", "units": 3.766}, {"amount": 0.03, "balance": null, "date": "2020-09-25", "description": "*** Stamp Duty ***", "dividend_rate": null, "nav": null, "type": "STAMP_DUTY_TAX", "units": null}, {"amount": 699.97, "balance": 19.584, "date": "2020-10-26", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 195.5818, "type": "PURCHASE_SIP", "units": 3.579}, {"amount": 0.03, "balance": null, "date": "2020-10-26", "description": "*** Stamp Duty ***", "dividend_rate": null, "nav": null, "type": "STAMP_DUTY_TAX", "units": null}, {"amount": 699.97, "balance": 22.927, "date": "2020-11-25", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 209.3876, "type": "PURCHASE_SIP", "units": 3.343}, {"amount": 0.03, "balance": null, "date": "2020-11-25", "description": "*** Stamp Duty ***", "dividend_rate": null, "nav": null, "type": "STAMP_DUTY_TAX", "units": null}, {"amount": 699.97, "balance": 26.007, "date": "2020-12-28", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 227.2644, "type": "PURCHASE_SIP", "units": 3.08}, {"amount": 0.03, "balance": null, "date": "2020-12-28", "description": "*** Stamp Duty ***", "dividend_rate": null, "nav": null, "type": "STAMP_DUTY_TAX", "units": null}, {"amount": -350.0, "balance": 24.554, "date": "2021-01-13", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 240.9493, "type": "REDEMPTION", "units": -1.453}, {"amount": 0.01, "balance": null, "date": "2021-01-13", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -262.38, "balance": 23.516, "date": "2021-02-17", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 252.7836, "type": "REDEMPTION", "units": -1.038}, {"amount": 0.01, "balance": null, "date": "2021-02-17", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": 100.0, "balance": 23.929, "date": "2021-03-30", "description": "Systematic Investment (1)", "dividend_rate": null, "nav": 242.2057, "type": "PURCHASE_SIP", "units": 0.413}, {"amount": -847.71, "balance": 20.429, "date": "2021-03-31", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 242.2057, "type": "REDEMPTION", "units": -3.5}, {"amount": 0.01, "balance": null, "date": "2021-03-31", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -302.83, "balance": 19.189, "date": "2021-04-29", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 244.2236, "type": "REDEMPTION", "units": -1.24}, {"amount": 0.01, "balance": null, "date": "2021-04-29", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -319.92, "balance": 17.937, "date": "2021-05-31", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 255.5346, "type": "REDEMPTION", "units": -1.252}, {"amount": 0.01, "balance": null, "date": "2021-05-31", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -297.78, "balance": 16.802, "date": "2021-06-28", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 262.3738, "type": "REDEMPTION", "units": -1.135}, {"amount": 0.01, "balance": null, "date": "2021-06-28", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -282.85, "balance": 15.729, "date": "2021-07-28", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 263.619, "type": "REDEMPTION", "units": -1.073}, {"amount": 0.01, "balance": null, "date": "2021-07-28", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -281.04, "balance": 14.656, "date": "2021-07-28", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 261.9292, "type": "REDEMPTION", "units": -1.073}, {"amount": 0.01, "balance": null, "date": "2021-07-28", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": 4074.36, "balance": 29.439, "date": "2021-08-20", "description": "Switch Over In", "dividend_rate": null, "nav": 275.6096, "type": "SWITCH_IN", "units": 14.783}, {"amount": 0.2, "balance": null, "date": "2021-08-20", "description": "*** Stamp Duty ***", "dividend_rate": null, "nav": null, "type": "STAMP_DUTY_TAX", "units": null}, {"amount": -132.78, "balance": 28.964, "date": "2021-08-26", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 279.5657, "type": "REDEMPTION", "units": -0.475}, {"amount": 0.01, "balance": null, "date": "2021-08-26", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -938.66, "balance": 25.726, "date": "2021-10-01", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 289.8915, "type": "REDEMPTION", "units": -3.238}, {"amount": 0.01, "balance": null, "date": "2021-10-01", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -1047.79, "balance": 22.147, "date": "2021-10-29", "description": "Redemption less TDS, STT", "dividend_rate": null, "nav": 292.7626, "type": "REDEMPTION", "units": -3.579}, {"amount": 0.01, "balance": null, "date": "2021-10-29", "description": "*** STT Paid ***", "dividend_rate": null, "nav": null, "type": "STT_TAX", "units": null}, {"amount": -1095.9, "balance": 18.276, "date": "2021-11-29", "description": "Redemption", "dividend_rate": null, "nav": 283.1064, "type": "REDEMPTION", "units": -3.871}, {"amount": -5181.46, "balance": 0.0, "date": "2021-12-24", "description": "Switch Over Out", "dividend_rate": null, "nav": 283.5119, "type": "SWITCH_OUT", "units": -18.276}], "type": "EQUITY", "valuation": {"date": "2021-12-24", "nav": 283.5119, "value": 0}}]}], "investor_info": {"address": "address", "email": "junit@email.com", "mobile": "9848022338", "name": "Junit"}, "statement_period": {"from": "01-Jan-1990", "to": "20-Jun-2023"}}
                """;
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
