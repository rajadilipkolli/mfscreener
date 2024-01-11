package com.learning.mfscreener.web.api;

import com.learning.mfscreener.models.response.XIRRResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface XIRRCalculatorApi {

    ResponseEntity<List<XIRRResponse>> getXIRR(
            @Parameter(description = "Pan of the end User", name = "pan", in = ParameterIn.PATH, example = "ABCDE1234F")
                    String panNumber);
}
