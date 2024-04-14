package com.learning.mfscreener.service;

import static com.learning.mfscreener.utils.AppConstants.FORMATTER_DD_MMM_YYYY;

import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.exception.NavNotFoundException;
import com.learning.mfscreener.exception.SchemeNotFoundException;
import com.learning.mfscreener.mapper.MfSchemeDtoToEntityMapper;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.models.projection.SchemeNameAndISIN;
import com.learning.mfscreener.repository.MFSchemeTypeRepository;
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
    private final MFSchemeTypeRepository mfSchemeTypeRepository;
    private final RestClient restClient;
    private final MfSchemeDtoToEntityMapper mfSchemeDtoToEntityMapper;
    private final UserSchemeDetailsService userSchemeDetailsService;

    public HistoricalNavService(
            SchemeService schemeService,
            MFSchemeTypeRepository mfSchemeTypeRepository,
            RestClient restClient,
            MfSchemeDtoToEntityMapper mfSchemeDtoToEntityMapper,
            UserSchemeDetailsService userSchemeDetailsService) {
        this.schemeService = schemeService;
        this.mfSchemeTypeRepository = mfSchemeTypeRepository;
        this.restClient = restClient;
        this.mfSchemeDtoToEntityMapper = mfSchemeDtoToEntityMapper;
        this.userSchemeDetailsService = userSchemeDetailsService;
    }

    public String getHistoricalNav(Long schemeCode, LocalDate navDate) {
        URI historicalNavUri = buildHistoricalNavUri(navDate);
        Optional<MFSchemeEntity> bySchemeCode = this.schemeService.findBySchemeCode(schemeCode);
        if (bySchemeCode.isPresent()) {
            return fetchAndProcessNavData(
                    historicalNavUri, bySchemeCode.get().getPayOut(), false, null, schemeCode, navDate);
        } else {
            return handleDiscontinuedScheme(schemeCode, historicalNavUri, navDate);
        }
    }

    String handleDiscontinuedScheme(Long schemeCode, URI historicalNavUri, LocalDate navDate) {
        // discontinued scheme Isin
        SchemeNameAndISIN schemeNameAndISIN = fetchSchemeDetails(schemeCode);
        String payOut = schemeNameAndISIN.getIsin();
        return fetchAndProcessNavData(historicalNavUri, payOut, true, schemeNameAndISIN, schemeCode, navDate);
    }

    String fetchAndProcessNavData(
            URI historicalNavUri,
            String payOut,
            boolean persistSchemeInfo,
            SchemeNameAndISIN schemeNameAndISIN,
            Long schemeCode,
            LocalDate navDate) {
        try {
            Reader inputString = fetchHistoricalNavByCallingUri(historicalNavUri);
            return parseNavData(inputString, payOut, persistSchemeInfo, schemeNameAndISIN, schemeCode, navDate);
        } catch (ResourceAccessException exception) {
            // eating as we can't do much, and it should be set when available
            LOGGER.error("Unable to load Historical Data, downstream service is down ", exception);
            return null;
        }
    }

    String parseNavData(
            Reader inputString,
            String payOut,
            boolean persistSchemeInfo,
            SchemeNameAndISIN schemeNameAndISIN,
            Long schemeCode,
            LocalDate navDate) {
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
                boolean nonAmcRow = true;
                boolean processRowByForce = false;
                if (tokenize.length == 1) {
                    nonAmcRow = false;
                    String tempVal = lineValue;
                    lineValue = br.readLine();
                    if (!StringUtils.hasText(lineValue)) {
                        lineValue = br.readLine();
                        tokenize = lineValue.split(AppConstants.NAV_SEPARATOR);
                        if (tokenize.length == 1) {
                            schemeType = tempVal;
                            amc = lineValue;
                        } else {
                            amc = tempVal;
                            processRowByForce = true;
                        }
                    }
                }
                if (nonAmcRow || processRowByForce) {
                    final String schemecode = tokenize[0];
                    final String payout = tokenize[2];
                    if (payOut.equalsIgnoreCase(payout)) {
                        oldSchemeId = schemecode;
                        if (persistSchemeInfo) {
                            String schemename = tokenize[1];
                            String nav = tokenize[4];
                            String date = tokenize[7];
                            final MFSchemeDTO mfSchemeDTO = new MFSchemeDTO(
                                    amc, Long.valueOf(schemecode), payout, schemename, nav, date, schemeType);
                            MFSchemeEntity mfSchemeEntity = mfSchemeDtoToEntityMapper.mapMFSchemeDTOToMFSchemeEntity(
                                    mfSchemeDTO, mfSchemeTypeRepository);
                            schemeService.saveEntity(mfSchemeEntity);
                        }
                    }
                }
                lineValue = br.readLine();
                if (!StringUtils.hasText(lineValue)) {
                    lineValue = br.readLine();
                }
            }
        } catch (IOException e) {
            LOGGER.error("Exception Occurred while reading response", e);
            throw new NavNotFoundException("Unable to parse for %s".formatted(schemeCode), navDate);
        }
        if (!StringUtils.hasText(oldSchemeId) && schemeNameAndISIN != null) {
            // Manually creating entry in mf_scheme table as no entry found in historical link
            MFSchemeEntity mfSchemeEntity = new MFSchemeEntity();
            mfSchemeEntity.setPayOut(payOut);
            mfSchemeEntity.setSchemeId(schemeCode);
            mfSchemeEntity.setSchemeName(schemeNameAndISIN.getScheme());
            schemeService.saveEntity(mfSchemeEntity);
            oldSchemeId = String.valueOf(schemeCode);
        } else {
            LOGGER.info("No Nav found for the given day");
        }
        return oldSchemeId;
    }

    StringReader fetchHistoricalNavByCallingUri(URI historicalNavUri) {
        String allNAVsByDate = restClient.get().uri(historicalNavUri).retrieve().body(String.class);
        return new StringReader(Objects.requireNonNull(allNAVsByDate));
    }

    SchemeNameAndISIN fetchSchemeDetails(Long schemeCode) {
        return userSchemeDetailsService
                .findFirstBySchemeCode(schemeCode)
                .orElseThrow(() -> new SchemeNotFoundException("Fund with schemeCode " + schemeCode + " Not Found"));
    }

    URI buildHistoricalNavUri(LocalDate navDate) {
        // URL https://portal.amfiindia.com/DownloadNAVHistoryReport_Po.aspx?tp=1&frmdt=01-Jan-2024&todt=03-Jan-2024
        String toDate = navDate.format(FORMATTER_DD_MMM_YYYY);
        String fromDate = navDate.minusDays(3).format(FORMATTER_DD_MMM_YYYY);
        // tp=3 Interval Fund Schemes ( Income )
        // tp=2 Close Ended Schemes ( Income )
        // tp=1 Open Ended Schemes
        String historicalUrl = "https://portal.amfiindia.com/DownloadNAVHistoryReport_Po.aspx?frmdt=%s&todt=%s"
                .formatted(fromDate, toDate);
        return UriComponentsBuilder.fromHttpUrl(historicalUrl).build().toUri();
    }
}
