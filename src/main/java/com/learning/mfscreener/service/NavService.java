package com.learning.mfscreener.service;

import com.learning.mfscreener.adapter.ConversionServiceAdapter;
import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.exception.NavNotFoundException;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.repository.MFSchemeRepository;
import com.learning.mfscreener.utils.LocalDateUtility;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class NavService {

    private final MFSchemeRepository mfSchemesRepository;
    private final ConversionServiceAdapter conversionServiceAdapter;
    private final SchemeService schemeService;
    private final HistoricalNavService historicalNavService;

    @Loggable
    public MFSchemeDTO getNav(Long schemeCode) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        // NAVs are refreshed only after 11:30 PM so reduce the day by 1
        if (currentDateTime.toLocalTime().isBefore(LocalTime.of(23, 30))) {
            currentDateTime = currentDateTime.minusDays(1);
        }

        LocalDate currentDate = currentDateTime.toLocalDate();

        return getNavByDateWithRetry(schemeCode, currentDate);
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
                mfSchemeDTO = getNavForDate(schemeCode, navDate);
                break; // Exit the loop if successful
            } catch (NavNotFoundException navNotFoundException) {
                log.error("NavNotFoundException occurred: {}", navNotFoundException.getMessage());

                if (retryCount == 1 || retryCount == 3) {
                    // make a call to get historical Data and persist
                    String oldSchemecode = historicalNavService.getHistoricalNav(schemeCode, navDate);
                    if (StringUtils.hasText(oldSchemecode)) {
                        schemeService.fetchSchemeDetails(oldSchemecode, schemeCode);
                    } else {
                        // NFO scenario where data is not present in historical data, hence load all available data
                        schemeService.fetchSchemeDetails(String.valueOf(schemeCode), schemeCode);
                    }
                }

                if (retryCount >= 4) {
                    throw navNotFoundException;
                }

                retryCount++;
                navDate = navNotFoundException.getDate().minusDays(1);
                log.info("Retrying for date: {} for scheme: {}", navDate, schemeCode);
            }
        }

        return mfSchemeDTO;
    }

    MFSchemeDTO getNavForDate(Long schemeCode, LocalDate navDate) {
        return this.mfSchemesRepository
                .findBySchemeIdAndMfSchemeNavEntities_NavDate(schemeCode, navDate)
                .map(conversionServiceAdapter::mapMFSchemeEntityToMFSchemeDTO)
                .orElseGet(() -> getSchemeDetails(schemeCode, navDate));
    }

    MFSchemeDTO getSchemeDetails(Long schemeCode, LocalDate navDate) {
        log.info("Fetching Nav for SchemeCode :{} for date :{} from Server", schemeCode, navDate);
        schemeService.fetchSchemeDetails(schemeCode);
        log.info("Fetched Nav for SchemeCode :{} for date :{} from Server", schemeCode, navDate);
        return this.mfSchemesRepository
                .findBySchemeIdAndMfSchemeNavEntities_NavDate(schemeCode, navDate)
                .map(conversionServiceAdapter::mapMFSchemeEntityToMFSchemeDTO)
                .orElseThrow(() -> new NavNotFoundException("Nav Not Found for schemeCode - " + schemeCode, navDate));
    }
}
