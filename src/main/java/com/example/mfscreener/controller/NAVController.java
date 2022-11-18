/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.controller;

import com.example.mfscreener.models.MFSchemeDTO;
import com.example.mfscreener.models.projection.FundDetailProjection;
import com.example.mfscreener.models.response.PortfolioResponse;
import com.example.mfscreener.service.NavService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class NAVController {

    private final NavService navService;

    @GetMapping(path = "/nav/{schemeCode}")
    @Operation(summary = "Fetch the latest NAV from AMFI website.")
    public MFSchemeDTO getScheme(
            @Parameter(description = "scheme Code for mutual fund", example = "120503")
                    @PathVariable(value = "schemeCode")
                    Long schemeCode) {

        return navService.getNav(schemeCode);
    }

    @GetMapping(path = "/nav/{schemeCode}/{date}")
    @Operation(
            summary = "Fetch NAV on date DD-MM-YYYY (or the last working day before DD-MM-YYYY).")
    public MFSchemeDTO getSchemeNavOnDate(
            @Parameter(description = "scheme Code for mutual fund", example = "120503")
                    @PathVariable
                    Long schemeCode,
            @Parameter(description = "date", example = "20-01-2020") @PathVariable String date) {
        return navService.getNavOnDate(schemeCode, date);
    }

    @GetMapping(path = "/scheme/{schemeName}")
    @Operation(summary = "Fetches the schemes matching key.")
    public List<FundDetailProjection> fetchSchemes(
            @Parameter(description = "scheme name for mutual fund", example = "sbi small cap")
                    @PathVariable(value = "schemeName")
                    String schemeName) {

        return navService.fetchSchemes(schemeName);
    }

    @GetMapping(path = "/schemes/{fundName}")
    @Operation(summary = "Fetches the schemes matching fund House.")
    public List<FundDetailProjection> fetchSchemesByFundName(
            @Parameter(
                            description = "fund house name for mutual funds",
                            example = "Mirae Asset Mutual fund")
                    @PathVariable(value = "fundName")
                    String fundName) {

        return navService.fetchSchemesByFundName(fundName);
    }

    @GetMapping("/portfolio/{pan}")
    @Operation(
            summary =
                    "Fetches the portfolio by Pan and given date, if date is empty then current"
                            + " date portfolio will be returned")
    public PortfolioResponse getPortfolio(
            @PathVariable("pan") String panNumber,
            @RequestParam(value = "date", required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate date) {
        return navService.getPortfolioByPAN(panNumber, date);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Persists the transaction details.")
    public String upload(@RequestPart("file") MultipartFile multipartFile) throws IOException {
        return navService.upload(multipartFile);
    }
}
