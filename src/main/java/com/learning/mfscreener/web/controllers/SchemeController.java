package com.learning.mfscreener.web.controllers;

import com.learning.mfscreener.models.projection.FundDetailProjection;
import com.learning.mfscreener.service.SchemeService;
import com.learning.mfscreener.web.api.SchemeApi;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scheme")
public class SchemeController implements SchemeApi {

    private final SchemeService schemeService;

    @Override
    @GetMapping(path = "/{schemeName}")
    public ResponseEntity<List<FundDetailProjection>> fetchSchemes(
            @PathVariable(value = "schemeName") String schemeName) {
        return ResponseEntity.ok(schemeService.fetchSchemes(schemeName));
    }

    @Override
    @GetMapping(path = "/fund/{fundName}")
    public ResponseEntity<List<FundDetailProjection>> fetchSchemesByFundName(
            @PathVariable(value = "fundName") String fundName) {
        return ResponseEntity.ok(schemeService.fetchSchemesByFundName(fundName));
    }
}
