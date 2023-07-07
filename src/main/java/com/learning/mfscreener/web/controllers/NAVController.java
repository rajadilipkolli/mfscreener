package com.learning.mfscreener.web.controllers;

import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.service.NavService;
import com.learning.mfscreener.web.api.NAVApi;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nav")
public class NAVController implements NAVApi {

    private final NavService navService;

    @Override
    @GetMapping(path = "/{schemeCode}")
    public ResponseEntity<MFSchemeDTO> getScheme(@PathVariable(name = "schemeCode") Long schemeCode) {
        return ResponseEntity.ok(navService.getNav(schemeCode));
    }

    @Override
    @GetMapping(path = "/{schemeCode}/{date}")
    public ResponseEntity<MFSchemeDTO> getSchemeNavOnDate(
            @PathVariable(name = "schemeCode") Long schemeCode,
            @PathVariable(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(navService.getNavOnDate(schemeCode, date));
    }
}
