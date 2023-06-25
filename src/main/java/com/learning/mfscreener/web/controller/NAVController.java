package com.learning.mfscreener.web.controller;

import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.service.NavService;
import com.learning.mfscreener.web.api.NAVApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NAVController implements NAVApi {
    private final NavService navService;

    @Override
    @GetMapping(path = "/nav/{schemeCode}")
    public ResponseEntity<MFSchemeDTO> getScheme(@PathVariable(value = "schemeCode") Long schemeCode) {
        return ResponseEntity.ok(navService.getNav(schemeCode));
    }
}
