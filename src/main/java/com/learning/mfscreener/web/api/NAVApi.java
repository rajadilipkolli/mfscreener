package com.learning.mfscreener.web.api;

import com.learning.mfscreener.models.MFSchemeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;

public interface NAVApi {

    @Operation(summary = "Fetch the latest NAV from AMFI website.")
    ResponseEntity<MFSchemeDTO> getScheme(
            @Parameter(
                            description = "scheme Code for mutual fund",
                            name = "schemeCode",
                            in = ParameterIn.PATH,
                            example = "120503")
                    Long schemeCode);

    @Operation(summary = "Fetch NAV on date DD-MM-YYYY (or the last working day before DD-MM-YYYY).")
    public ResponseEntity<MFSchemeDTO> getSchemeNavOnDate(
            @Parameter(description = "scheme Code for mutual fund", in = ParameterIn.PATH, example = "120503")
                    Long schemeCode,
            @Parameter(description = "date", in = ParameterIn.PATH, example = "2020-20-01") LocalDate date);
}
