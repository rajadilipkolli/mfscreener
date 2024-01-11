package com.learning.mfscreener.web.controllers;

import com.learning.mfscreener.models.response.XIRRResponse;
import com.learning.mfscreener.service.CalculatorService;
import com.learning.mfscreener.web.api.XIRRCalculatorApi;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class XIRRCalculatorController implements XIRRCalculatorApi {

    private final CalculatorService calculatorService;

    public XIRRCalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    // endpoint to calculate XIRR for a given pan
    @GetMapping("/xirr/{pan}")
    @Override
    public ResponseEntity<List<XIRRResponse>> getXIRR(@PathVariable String pan) {
        return ResponseEntity.ok(calculatorService.calculateTotalXIRRByPan(pan));
    }
}
