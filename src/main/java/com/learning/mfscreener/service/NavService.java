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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NavService {

    private final MFSchemeRepository mfSchemesRepository;
    private final ConversionServiceAdapter conversionServiceAdapter;

    @Loggable
    public MFSchemeDTO getNav(Long schemeCode) {
        return mfSchemesRepository
                .findBySchemeIdAndMfSchemeNavEntities_NavDate(
                        schemeCode, LocalDateUtility.getAdjustedDate(LocalDate.now()))
                .map(conversionServiceAdapter::mapMFSchemeEntityToMFSchemeDTO)
                .orElseThrow(() -> new SchemeNotFoundException(String.format("Scheme %s Not Found", schemeCode)));
    }
}
