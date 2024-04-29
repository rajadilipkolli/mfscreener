package com.learning.mfscreener.service;

import static com.learning.mfscreener.utils.AppConstants.FORMATTER_DD_MMM_YYYY;

import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.entities.MFSchemeNavEntity;
import com.learning.mfscreener.exception.NavNotFoundException;
import com.learning.mfscreener.mapper.MfSchemeDtoToEntityMapper;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.models.projection.SchemeNameAndISIN;
import com.learning.mfscreener.utils.AppConstants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Loggable
public class HistoricalNavService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoricalNavService.class);

    private final SchemeService schemeService;
    private final RestClient restClient;
    private final MfSchemeDtoToEntityMapper mfSchemeDtoToEntityMapper;
    private final UserSchemeDetailsService userSchemeDetailsService;

    public HistoricalNavService(
            SchemeService schemeService,
            RestClient restClient,
            MfSchemeDtoToEntityMapper mfSchemeDtoToEntityMapper,
            UserSchemeDetailsService userSchemeDetailsService) {
        this.schemeService = schemeService;
        this.restClient = restClient;
        this.mfSchemeDtoToEntityMapper = mfSchemeDtoToEntityMapper;
        this.userSchemeDetailsService = userSchemeDetailsService;
    }

    public String getHistoricalNav(Long schemeCode, LocalDate navDate) {
        String toDate = navDate.format(FORMATTER_DD_MMM_YYYY);
        String fromDate = navDate.minusDays(3).format(FORMATTER_DD_MMM_YYYY);
        URI historicalNavUri = buildHistoricalNavUri(toDate, fromDate);
        Optional<MFSchemeEntity> bySchemeCode = this.schemeService.findBySchemeCode(schemeCode);
        if (bySchemeCode.isPresent()) {
            return fetchAndProcessNavData(historicalNavUri, bySchemeCode.get().getPayOut(), false, schemeCode, navDate);
        } else {
            return handleDiscontinuedScheme(schemeCode, historicalNavUri, navDate);
        }
    }

    public String getHistoricalGrandFatheredValue(String isin, LocalDate grandFatheredDate) {
        String toDate = grandFatheredDate.format(FORMATTER_DD_MMM_YYYY);
        URI historicalNavUri = buildHistoricalNavUri(toDate, toDate);
        return fetchAndProcessNavData(historicalNavUri, isin, true, null, grandFatheredDate);
    }

    String handleDiscontinuedScheme(Long schemeCode, URI historicalNavUri, LocalDate navDate) {
        // discontinued scheme Isin
        Optional<SchemeNameAndISIN> schemeNameAndISIN = userSchemeDetailsService.findFirstBySchemeCode(schemeCode);
        String payOut = null;
        if (schemeNameAndISIN.isPresent()) {
            payOut = schemeNameAndISIN.get().getIsin();
        }
        return fetchAndProcessNavData(historicalNavUri, payOut, true, schemeCode, navDate);
    }

    String fetchAndProcessNavData(
            URI historicalNavUri, String payOut, boolean persistSchemeInfo, Long schemeCode, LocalDate navDate) {
        try {
            String allNAVsByDate = fetchHistoricalNavData(historicalNavUri);
            Reader inputString = new StringReader(Objects.requireNonNull(allNAVsByDate));
            return parseNavData(inputString, payOut, persistSchemeInfo, schemeCode, navDate);
        } catch (ResourceAccessException exception) {
            // eating as we can't do much, and it should be set when available
            LOGGER.error("Unable to load Historical Data, downstream service is down ", exception);
            return null;
        }
    }

    String parseNavData(
            Reader inputString, String payOut, boolean persistSchemeInfo, Long schemeCode, LocalDate navDate) {
        String oldSchemeId = null;
        try (BufferedReader br = new BufferedReader(inputString)) {
            String lineValue = br.readLine();
            for (int i = 0; i < 2; ++i) {
                lineValue = br.readLine();
            }
            String schemeType = lineValue;
            String amc = lineValue;
            while (lineValue != null && !StringUtils.hasText(oldSchemeId)) {
                String[] tokenize = lineValue.split(AppConstants.NAV_SEPARATOR);
                if (tokenize.length == 1) {
                    String tempVal = lineValue;
                    lineValue = readNextNonEmptyLine(br);
                    tokenize = lineValue.split(AppConstants.NAV_SEPARATOR);
                    if (tokenize.length == 1) {
                        schemeType = tempVal;
                        amc = lineValue;
                    } else {
                        amc = tempVal;
                        oldSchemeId = handleMultipleTokenLine(
                                payOut, persistSchemeInfo, tokenize, oldSchemeId, amc, schemeType, schemeCode);
                    }

                } else {
                    oldSchemeId = handleMultipleTokenLine(
                            payOut, persistSchemeInfo, tokenize, oldSchemeId, amc, schemeType, schemeCode);
                }
                lineValue = readNextNonEmptyLine(br);
            }
        } catch (IOException e) {
            LOGGER.error("Exception Occurred while reading response", e);
            throw new NavNotFoundException("Unable to parse for %s".formatted(schemeCode), navDate);
        }
        return oldSchemeId;
    }

    String readNextNonEmptyLine(BufferedReader br) throws IOException {
        String lineValue = br.readLine();
        while (lineValue != null && !StringUtils.hasText(lineValue)) {
            lineValue = br.readLine();
        }
        return lineValue;
    }

    String handleMultipleTokenLine(
            String payOut,
            boolean persistSchemeInfo,
            String[] tokenize,
            String oldSchemeId,
            String amc,
            String schemeType,
            Long inputSchemeCode) {
        final String schemeCode = tokenize[0];
        final String payout = tokenize[2];
        if (payout.equalsIgnoreCase(payOut) || schemeCode.equalsIgnoreCase(inputSchemeCode.toString())) {
            oldSchemeId = schemeCode;
            if (persistSchemeInfo) {
                persistSchemeInfo(tokenize, amc, schemeType, schemeCode, payout);
            }
        }
        return oldSchemeId;
    }

    void persistSchemeInfo(String[] tokenize, String amc, String schemeType, String schemeCode, String payout) {
        MFSchemeEntity mfSchemeEntity = createSchemeDTO(tokenize, amc, schemeType, schemeCode, payout);
        MFSchemeEntity persistedScheme = schemeService.saveEntity(mfSchemeEntity);
        LOGGER.debug("Persisted Entity :{}", persistedScheme);
    }

    MFSchemeEntity createSchemeDTO(String[] tokenize, String amc, String schemeType, String schemeCode, String payout) {
        Optional<MFSchemeEntity> bySchemeCode = schemeService.findBySchemeCode(Long.valueOf(schemeCode));
        String nav = tokenize[4];
        String date = tokenize[7];
        if (bySchemeCode.isEmpty()) {
            String schemeName = tokenize[1];
            MFSchemeDTO mfSchemeDTO =
                    new MFSchemeDTO(amc, Long.valueOf(schemeCode), payout, schemeName, nav, date, schemeType);
            return mfSchemeDtoToEntityMapper.mapMFSchemeDTOToMFSchemeEntity(mfSchemeDTO);
        } else {
            MFSchemeNavEntity mfSchemenavEntity = new MFSchemeNavEntity();
            mfSchemenavEntity.setNav("N.A.".equals(nav) ? 0F : Float.parseFloat(nav));
            mfSchemenavEntity.setNavDate(LocalDate.parse(date, AppConstants.FORMATTER_DD_MMM_YYYY));
            return bySchemeCode.get().addSchemeNav(mfSchemenavEntity);
        }
    }

    String fetchHistoricalNavData(URI historicalNavUri) {
        return restClient.get().uri(historicalNavUri).retrieve().body(String.class);
    }

    URI buildHistoricalNavUri(String toDate, String fromDate) {
        // URL https://portal.amfiindia.com/DownloadNAVHistoryReport_Po.aspx?tp=1&frmdt=01-Jan-2024&todt=03-Jan-2024
        // tp=3 Interval Fund Schemes ( Income )
        // tp=2 Close Ended Schemes ( Income )
        // tp=1 Open Ended Schemes
        String historicalUrl = "https://portal.amfiindia.com/DownloadNAVHistoryReport_Po.aspx?frmdt=%s&todt=%s"
                .formatted(fromDate, toDate);
        return UriComponentsBuilder.fromHttpUrl(historicalUrl).build().toUri();
    }
}
