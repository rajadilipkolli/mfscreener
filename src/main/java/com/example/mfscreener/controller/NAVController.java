/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.controller;

import com.example.mfscreener.models.PortfolioDTO;
import com.example.mfscreener.models.Scheme;
import com.example.mfscreener.models.projection.FundDetailProjection;
import com.example.mfscreener.service.NavService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NAVController {

    private final NavService navService;

    @GetMapping(path = "/nav/{schemeCode}")
    @Operation(summary = "Fetch the latest NAV from AMFI website.")
    public Scheme getScheme(
            @Parameter(description = "scheme Code for mutual fund", example = "120503")
                    @PathVariable(value = "schemeCode")
                    Long schemeCode) {

        return navService.getNav(schemeCode);
    }

    @GetMapping(path = "/nav/{schemeCode}/{date}")
    @Operation(
            summary = "Fetch NAV on date DD-MM-YYYY (or the last working day before DD-MM-YYYY).")
    public Scheme getSchemeNavOnDate(
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

    @GetMapping("/portfolio")
    public PortfolioDTO getPortfolio() {
        return navService.getPortfolio();
    }
}
