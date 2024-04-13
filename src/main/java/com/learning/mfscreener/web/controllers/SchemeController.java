package com.learning.mfscreener.web.controllers;

import com.learning.mfscreener.models.projection.FundDetailProjection;
import com.learning.mfscreener.service.SchemeService;
import com.learning.mfscreener.web.api.SchemeApi;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scheme")
public class SchemeController implements SchemeApi {

    private final SchemeService schemeService;

    public SchemeController(SchemeService schemeService) {
        this.schemeService = schemeService;
    }

    @Override
    @GetMapping(path = "/{schemeName}")
    public ResponseEntity<List<FundDetailProjection>> fetchSchemes(@PathVariable("schemeName") String schemeName) {
        return ResponseEntity.ok(schemeService.fetchSchemes(schemeName));
    }

    @Override
    @GetMapping(path = "/fund/{fundName}")
    public ResponseEntity<List<FundDetailProjection>> fetchSchemesByFundName(
            @PathVariable("fundName") String fundName) {
        return ResponseEntity.ok(schemeService.fetchSchemesByFundName(fundName));
    }
}
