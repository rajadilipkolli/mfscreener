package com.learning.mfscreener.web.api;

import com.learning.mfscreener.models.response.PortfolioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.constraints.PastOrPresent;
import java.io.IOException;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

public interface PortfolioApi {

    @Operation(summary = "Persists the transaction details.")
    ResponseEntity<String> upload(@RequestPart("file") MultipartFile multipartFile) throws IOException;

    @Operation(
            summary =
                    """
                    Fetches the portfolio by Pan and given date, if date is empty then current
                    date portfolio will be returned
                    """)
    ResponseEntity<PortfolioResponse> getPortfolio(
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
