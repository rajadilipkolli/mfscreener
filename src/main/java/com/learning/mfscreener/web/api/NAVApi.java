package com.learning.mfscreener.web.api;

import com.learning.mfscreener.models.MFSchemeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;

@Tag(name = "nav")
public interface NAVApi {

    @Operation(summary = "Fetch the latest NAV from AMFI website.")
    ResponseEntity<MFSchemeDTO> getScheme(
            @Parameter(
                            description = "scheme Code for mutual fund",
                            name = "schemeCode",
                            in = ParameterIn.PATH,
                            example = "120503")
                    Long schemeCode);

    @Operation(summary = "Fetch NAV on date yyyy-MM-dd (or the last working day before yyyy-MM-dd).")
    ResponseEntity<MFSchemeDTO> getSchemeNavOnDate(
            @Parameter(description = "scheme Code for mutual fund", in = ParameterIn.PATH, example = "120503")
                    @Min(value = 100000, message = "Min value of schemeCode should be greater than 100000")
                    @Max(value = 160000, message = "Max value of schemeCode should be less than 160000")
                    @Valid
                    Long schemeCode,
            @Parameter(description = "date", in = ParameterIn.PATH, example = "2023-12-31") LocalDate date);
}
