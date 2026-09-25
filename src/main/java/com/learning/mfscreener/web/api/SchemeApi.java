package com.learning.mfscreener.web.api;

import com.learning.mfscreener.models.projection.FundDetailProjection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface SchemeApi {

    @Operation(summary = "Fetches the schemes matching key.")
    ResponseEntity<List<FundDetailProjection>> fetchSchemes(
            @Parameter(
                            description = "scheme name for mutual fund",
                            name = "schemeName",
                            in = ParameterIn.PATH,
                            example = "sbi small cap")
                    String schemeName);

    @Operation(summary = "Fetches the schemes matching fund House.")
    ResponseEntity<List<FundDetailProjection>> fetchSchemesByFundName(
            @Parameter(
                            description = "fund house name for mutual funds",
                            name = "fundName",
                            in = ParameterIn.PATH,
                            example = "Mirae Asset Mutual fund")
                    String fundName);
}
