package com.learning.mfscreener.service;

import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.exception.NavNotFoundException;
import com.learning.mfscreener.models.MFSchemeDTO;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CachedNavService {

    private static final Logger log = LoggerFactory.getLogger(CachedNavService.class);

    private final SchemeService schemeService;

    public CachedNavService(SchemeService schemeService) {
        this.schemeService = schemeService;
    }

    @Cacheable(cacheNames = "getNavForDate", unless = "#result == null")
    @Loggable
    public MFSchemeDTO getNavForDate(Long schemeCode, LocalDate navDate) {
        return schemeService
                .getMfSchemeDTO(schemeCode, navDate)
                .orElseGet(() -> fetchAndGetSchemeDetails(schemeCode, navDate));
    }

    MFSchemeDTO fetchAndGetSchemeDetails(Long schemeCode, LocalDate navDate) {
        log.info("Fetching Nav for SchemeCode :{} for date :{} from Server", schemeCode, navDate);
        schemeService.fetchSchemeDetails(schemeCode);
        log.info("Fetched Nav for SchemeCode :{} for date :{} from Server", schemeCode, navDate);
        return schemeService
                .getMfSchemeDTO(schemeCode, navDate)
                .orElseThrow(() -> new NavNotFoundException("Nav Not Found for schemeCode - " + schemeCode, navDate));
    }
}
