package com.learning.mfscreener.service;

import com.learning.mfscreener.adapter.ConversionServiceAdapter;
import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.exception.SchemeNotFoundException;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.repository.MFSchemeRepository;
import com.learning.mfscreener.utils.LocalDateUtility;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NavService {

    private final MFSchemeRepository mfSchemesRepository;
    private final ConversionServiceAdapter conversionServiceAdapter;

    @Loggable
    public MFSchemeDTO getNav(Long schemeCode) {
        return mfSchemesRepository
                .findBySchemeIdAndNavDate(schemeCode, LocalDateUtility.getAdjustedDate(LocalDate.now()))
                .map(conversionServiceAdapter::mapMFSchemeEntityToMFSchemeDTO)
                .orElseThrow(() -> new SchemeNotFoundException("Scheme Not Found"));
    }
}
