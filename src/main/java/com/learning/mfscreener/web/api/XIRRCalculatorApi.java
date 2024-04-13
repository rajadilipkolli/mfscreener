package com.learning.mfscreener.web.api;

import com.learning.mfscreener.models.response.XIRRResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface XIRRCalculatorApi {

    ResponseEntity<List<XIRRResponse>> getXIRR(
            @Parameter(description = "Pan of the end User", name = "pan", in = ParameterIn.PATH, example = "ABCDE1234F")
                    String panNumber,
            @Parameter(
                            description = "get portfolio value for given date (yyyy-MM-dd) format",
                            in = ParameterIn.QUERY,
                            name = "asOfDate",
                            example = "2023-12-31")
                    @PastOrPresent(message = "Date should be past or today")
                    LocalDate asOfDate);
}
