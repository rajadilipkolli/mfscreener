package com.learning.mfscreener.service;

import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.exception.NavNotFoundException;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.utils.AppConstants;
import com.learning.mfscreener.utils.LocalDateUtility;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Loggable
public class NavService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NavService.class);

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
    public BigDecimal getNavByISINOnDate(String isin, LocalDate inputDate) {
        Optional<Long> schemeIdByISIN = schemeService.getSchemeIdByISIN(isin);
        if (schemeIdByISIN.isPresent()) {
            MFSchemeDTO navByDateWithRetry = getNavByDateWithRetry(schemeIdByISIN.get(), inputDate);
            return BigDecimal.valueOf(Long.parseLong(navByDateWithRetry.nav()));
        }
        return BigDecimal.ZERO;
    }

    @Loggable
    public MFSchemeDTO getNavByDateWithRetry(Long schemeCode, LocalDate navDate) {
        LOGGER.info("Fetching Nav for AMFISchemeCode: {} for date: {} from Cache", schemeCode, navDate);
        MFSchemeDTO mfSchemeDTO;
        int retryCount = 0;

        while (true) {
            try {
                mfSchemeDTO = cachedNavService.getNavForDate(schemeCode, navDate);
                break; // Exit the loop if successful
            } catch (NavNotFoundException navNotFoundException) {
                LOGGER.error("NavNotFoundException occurred: {}", navNotFoundException.getMessage());

                LocalDate currentNavDate = navNotFoundException.getDate();
                if (retryCount == AppConstants.FIRST_RETRY || retryCount == AppConstants.THIRD_RETRY) {
                    // make a call to get historical Data and persist
                    String oldSchemeCode = historicalNavService.getHistoricalNav(schemeCode, navDate);
                    if (StringUtils.hasText(oldSchemeCode)) {
                        schemeService.fetchSchemeDetails(oldSchemeCode, schemeCode);
                        currentNavDate = LocalDateUtility.getAdjustedDate(currentNavDate.plusDays(2));
                    } else {
                        // NFO scenario where data is not present in historical data, hence load all available data
                        schemeService.fetchSchemeDetails(String.valueOf(schemeCode), schemeCode);
                    }
                }
                // retrying 4 times
                if (retryCount >= AppConstants.MAX_RETRIES) {
                    throw navNotFoundException;
                }

                retryCount++;
                navDate = LocalDateUtility.getAdjustedDate(currentNavDate.minusDays(1));
                LOGGER.info("Retrying for date: {} for scheme: {}", navDate, schemeCode);
            }
        }

        return mfSchemeDTO;
    }
}
