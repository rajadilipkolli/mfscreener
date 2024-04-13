package com.learning.mfscreener.web.controllers;

import com.learning.mfscreener.models.response.XIRRResponse;
import com.learning.mfscreener.service.XIRRCalculatorService;
import com.learning.mfscreener.web.api.XIRRCalculatorApi;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Validated
public class XIRRCalculatorController implements XIRRCalculatorApi {

    private final XIRRCalculatorService xIRRCalculatorService;

    public XIRRCalculatorController(XIRRCalculatorService xIRRCalculatorService) {
        this.xIRRCalculatorService = xIRRCalculatorService;
    }

    // endpoint to calculate XIRR for a given pan
    @GetMapping("/xirr/{pan}")
    @Override
    public ResponseEntity<List<XIRRResponse>> getXIRR(
            @PathVariable("pan") String panNumber,
            @RequestParam(required = false, name = "asOfDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate asOfDate) {
        return ResponseEntity.ok(xIRRCalculatorService.calculateTotalXIRRByPan(panNumber, asOfDate));
    }
}
