package com.learning.mfscreener.service;

import static com.learning.mfscreener.utils.AppConstants.FORMATTER_DD_MMM_YYYY;

import com.learning.mfscreener.adapter.ConversionServiceAdapter;
import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.entities.MFSchemeNavEntity;
import com.learning.mfscreener.exception.NavNotFoundException;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.models.NAVDataDTO;
import com.learning.mfscreener.repository.MFSchemeRepository;
import com.learning.mfscreener.utils.AppConstants;
import com.learning.mfscreener.utils.LocalDateUtility;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class NavService {

    private final MFSchemeRepository mfSchemesRepository;
    private final ConversionServiceAdapter conversionServiceAdapter;
    private final SchemeService schemeService;
    private final RestClient restClient;

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

                if (retryCount >= 3) {
                    // make a call to get historical Data and persist
                    MFSchemeEntity historicalNav = getHistoricalNav(schemeCode, navDate);
                    if (historicalNav != null) {
                        return conversionServiceAdapter.mapMFSchemeEntityToMFSchemeDTO(historicalNav);
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

    MFSchemeEntity getHistoricalNav(Long schemeCode, LocalDate navDate) {
        // URL https://portal.amfiindia.com/DownloadNAVHistoryReport_Po.aspx?tp=1&frmdt=01-Jan-2024&todt=03-Jan-2024
        String toDate = navDate.format(FORMATTER_DD_MMM_YYYY);
        String fromDate = navDate.minusDays(3).format(FORMATTER_DD_MMM_YYYY);
        String historicalUrl = "https://portal.amfiindia.com/DownloadNAVHistoryReport_Po.aspx?tp=1&frmdt=%s&todt=%s"
                .formatted(fromDate, toDate);
        MFSchemeEntity bySchemeId =
                mfSchemesRepository.findBySchemeId(schemeCode).orElse(null);
        String payOut = bySchemeId.getPayOut();
        URI uri = UriComponentsBuilder.fromHttpUrl(historicalUrl).build().toUri();
        String allNAVs = restClient.get().uri(uri).retrieve().body(String.class);
        Reader inputString = new StringReader(Objects.requireNonNull(allNAVs));
        try (BufferedReader br = new BufferedReader(inputString)) {
            String lineValue = br.readLine();
            for (int i = 0; i < 4; ++i) {
                lineValue = br.readLine();
            }
            MFSchemeEntity schemeEntity = null;
            while (lineValue != null) {
                int check = 0;
                final String[] tokenize = lineValue.split(AppConstants.SEPARATOR);
                if (tokenize.length == 1) {
                    check = 1;
                }
                if (check == 0) {
                    final String payout = tokenize[2];
                    final String nav = tokenize[4];
                    final String date = tokenize[7];
                    if (payOut.equalsIgnoreCase(payout) && date.equalsIgnoreCase(toDate)) {
                        final NAVDataDTO navDataDTO = new NAVDataDTO(
                                LocalDate.parse(date, FORMATTER_DD_MMM_YYYY), Float.valueOf(nav), schemeCode);
                        MFSchemeNavEntity mfSchemeNavEntity =
                                conversionServiceAdapter.mapNAVDataDTOToMFSchemeNavEntity(navDataDTO);
                        bySchemeId.addSchemeNav(mfSchemeNavEntity);
                        schemeEntity = mfSchemesRepository.save(bySchemeId);
                    }
                }
                lineValue = br.readLine();
                if (!StringUtils.hasText(lineValue)) {
                    lineValue = br.readLine();
                }
            }
            return schemeEntity;
        } catch (IOException e) {
            log.error("Exception Occured while reading response", e);
            throw new NavNotFoundException("Unable to parse for %s".formatted(schemeCode), navDate);
        }
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
