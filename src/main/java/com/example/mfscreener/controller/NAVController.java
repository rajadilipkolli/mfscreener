package com.example.mfscreener.controller;

import com.example.mfscreener.model.FundDetailDTO;
import com.example.mfscreener.model.PortfolioDetails;
import com.example.mfscreener.model.Scheme;
import com.example.mfscreener.service.NavService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

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
  @Operation(summary = "Fetch NAV on date DD-MM-YYYY (or the last working day before DD-MM-YYYY).")
  public Scheme getSchemeNavOnDate(
      @Parameter(description = "scheme Code for mutual fund", example = "120503") @PathVariable
          Long schemeCode,
      @Parameter(description = "date", example = "20-01-2020") @PathVariable String date) {
    return navService.getNavOnDate(schemeCode, date);
  }

  @GetMapping(path = "/scheme/{schemeName}")
  @Operation(summary = "Fetches the schemes matching key.")
  public List<FundDetailDTO> fetchSchemes(
      @Parameter(description = "scheme name for mutual fund", example = "sbi small cap")
          @PathVariable(value = "schemeName")
          String schemeName) {

    return navService.fetchSchemes(schemeName);
  }

  @GetMapping(path = "/schemes/{fundName}")
  @Operation(summary = "Fetches the schemes matching fund House.")
  public List<FundDetailDTO> fetchSchemesByFundName(
      @Parameter(
              description = "fund house name for mutual funds",
              example = "Mirae Asset Mutual fund")
          @PathVariable(value = "fundName")
          String fundName) {

    return navService.fetchSchemesByFundName(fundName);
  }

  @GetMapping("/upload")
  @Operation(summary = "Persists the transaction details.")
  public String upload() throws IOException {
    return navService.upload();
  }

  @PutMapping("/synonym/{schemeId}/{schemaName}")
  public String updateSynonym(@PathVariable String schemeId, @PathVariable String schemaName) {
    return navService.updateSynonym(schemeId, schemaName);
  }

  @GetMapping("/portfolio")
  public List<PortfolioDetails> getPortfolio(){
    return navService.getPortfolio();
  }
}
