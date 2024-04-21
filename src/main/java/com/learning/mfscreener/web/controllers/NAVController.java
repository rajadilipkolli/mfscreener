package com.learning.mfscreener.web.controllers;

import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.service.NavService;
import com.learning.mfscreener.web.api.NAVApi;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nav")
@Validated
public class NAVController implements NAVApi {

    private final NavService navService;

    public NAVController(NavService navService) {
        this.navService = navService;
    }

    @Override
    @GetMapping(path = "/{schemeCode}")
    public ResponseEntity<MFSchemeDTO> getScheme(@PathVariable("schemeCode") Long schemeCode) {
        return ResponseEntity.ok(navService.getNav(schemeCode));
    }

    @Override
    @GetMapping(path = "/{schemeCode}/{date}")
    public ResponseEntity<MFSchemeDTO> getSchemeNavOnDate(
            @PathVariable("schemeCode") Long schemeCode,
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(navService.getNavOnDate(schemeCode, date));
    }
}
