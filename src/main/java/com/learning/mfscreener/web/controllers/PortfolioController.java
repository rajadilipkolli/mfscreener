package com.learning.mfscreener.web.controllers;

import com.learning.mfscreener.models.response.PortfolioResponse;
import com.learning.mfscreener.models.response.ProcessCasResponse;
import com.learning.mfscreener.service.PortfolioService;
import com.learning.mfscreener.web.api.PortfolioApi;
import java.io.IOException;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/portfolio")
@Validated
public class PortfolioController implements PortfolioApi {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @Override
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProcessCasResponse> upload(@RequestPart("file") MultipartFile multipartFile)
            throws IOException {
        return ResponseEntity.ok(portfolioService.upload(multipartFile));
    }

    @GetMapping("/{pan}")
    @Override
    public ResponseEntity<PortfolioResponse> getPortfolio(
            @PathVariable("pan") String panNumber,
            @RequestParam(required = false, name = "asOfDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate asOfDate) {
        return ResponseEntity.ok(portfolioService.getPortfolioByPAN(panNumber, asOfDate));
    }
}
