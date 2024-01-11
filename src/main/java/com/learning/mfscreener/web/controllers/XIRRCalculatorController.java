package com.learning.mfscreener.web.controllers;

import com.learning.mfscreener.service.CalculatorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    // endpoint to calculate XIRR for a given pan
    @GetMapping("/xirr/{pan}")
    public double getXIRR(@PathVariable String pan) {
        return calculatorService.calculateTotalXIRRByPan(pan);
    }
}
