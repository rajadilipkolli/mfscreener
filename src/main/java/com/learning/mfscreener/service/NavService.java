package com.learning.mfscreener.service;

import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.exception.NavNotFoundException;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.utils.LocalDateUtility;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NavService {

    private static final Logger log = LoggerFactory.getLogger(NavService.class);

    private final HistoricalNavService historicalNavService;
    private final CachedNavService cachedNavService;
    private final SchemeService schemeService;

    public NavService(
            HistoricalNavService historicalNavService, CachedNavService cachedNavService, SchemeService schemeService) {
        this.historicalNavService = historicalNavService;
        this.cachedNavService = cachedNavService;
        this.schemeService = schemeService;
    }

    @Loggable
    public MFSchemeDTO getNav(Long schemeCode) {
        return getNavByDateWithRetry(schemeCode, LocalDateUtility.getAdjustedDate());
    }

    @Loggable
    public MFSchemeDTO getNavOnDate(Long schemeCode, LocalDate inputDate) {
        LocalDate adjustedDate = LocalDateUtility.getAdjustedDate(inputDate);
        return getNavByDateWithRetry(schemeCode, adjustedDate);
    }

    @Loggable
    public MFSchemeDTO getNavByDateWithRetry(Long schemeCode, LocalDate navDate) {
        log.info("Fetching Nav for AMFISchemeCode: {} for date: {} from Database", schemeCode, navDate);
        MFSchemeDTO mfSchemeDTO;
        int retryCount = 0;

        while (true) {
            try {
                mfSchemeDTO = cachedNavService.getNavForDate(schemeCode, navDate);
                break; // Exit the loop if successful
            } catch (NavNotFoundException navNotFoundException) {
                log.error("NavNotFoundException occurred: {}", navNotFoundException.getMessage());

                if (retryCount == 1 || retryCount == 3) {
                    // make a call to get historical Data and persist
                    String oldSchemeCode = historicalNavService.getHistoricalNav(schemeCode, navDate);
                    if (StringUtils.hasText(oldSchemeCode)) {
                        schemeService.fetchSchemeDetails(oldSchemeCode, schemeCode);
                    } else {
                        // NFO scenario where data is not present in historical data, hence load all available data
                        schemeService.fetchSchemeDetails(String.valueOf(schemeCode), schemeCode);
                    }
                }
                // retrying 4 times
                if (retryCount >= 4) {
                    throw navNotFoundException;
                }

                retryCount++;
                navDate = LocalDateUtility.getAdjustedDate(
                        navNotFoundException.getDate().minusDays(1));
                log.info("Retrying for date: {} for scheme: {}", navDate, schemeCode);
            }
        }

        return mfSchemeDTO;
    }
}
