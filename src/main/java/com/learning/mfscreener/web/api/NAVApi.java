package com.learning.mfscreener.web.api;

import com.learning.mfscreener.models.MFSchemeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
}
