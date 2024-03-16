package com.learning.mfscreener.service;

import static com.learning.mfscreener.utils.AppConstants.FORMATTER_DD_MMM_YYYY;

import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.exception.NavNotFoundException;
import com.learning.mfscreener.exception.SchemeNotFoundException;
import com.learning.mfscreener.mapper.MfSchemeDtoToEntityMapper;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.models.projection.SchemeNameAndISIN;
import com.learning.mfscreener.repository.MFSchemeRepository;
import com.learning.mfscreener.repository.UserSchemeDetailsEntityRepository;
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
public class HistoricalNavService {

    private final MFSchemeRepository mfSchemeRepository;
    private final UserSchemeDetailsEntityRepository userSchemeDetailsEntityRepository;
    private final RestClient restClient;
    private final MfSchemeDtoToEntityMapper mfSchemeDtoToEntityMapper;

    private static final Logger log = LoggerFactory.getLogger(HistoricalNavService.class);

    public HistoricalNavService(
            MFSchemeRepository mfSchemeRepository,
            RestClient restClient,
            UserSchemeDetailsEntityRepository userSchemeDetailsEntityRepository,
            MfSchemeDtoToEntityMapper mfSchemeDtoToEntityMapper) {
        this.mfSchemeRepository = mfSchemeRepository;
        this.restClient = restClient;
        this.userSchemeDetailsEntityRepository = userSchemeDetailsEntityRepository;
        this.mfSchemeDtoToEntityMapper = mfSchemeDtoToEntityMapper;
    }

    public String getHistoricalNav(Long schemeCode, LocalDate navDate) {
        // URL https://portal.amfiindia.com/DownloadNAVHistoryReport_Po.aspx?tp=1&frmdt=01-Jan-2024&todt=03-Jan-2024
        String toDate = navDate.format(FORMATTER_DD_MMM_YYYY);
        String fromDate = navDate.minusDays(3).format(FORMATTER_DD_MMM_YYYY);
        // tp=3 Interval Fund Schemes ( Income )
        // tp=2 Close Ended Schemes ( Income )
        // tp=1 Open Ended Schemes
        String historicalUrl = "https://portal.amfiindia.com/DownloadNAVHistoryReport_Po.aspx?frmdt=%s&todt=%s"
                .formatted(fromDate, toDate);
        Optional<MFSchemeEntity> bySchemeId = mfSchemeRepository.findBySchemeId(schemeCode);
        String payOut;
        boolean persistSchemeInfo = false;
        SchemeNameAndISIN firstByAmfi = null;
        if (bySchemeId.isEmpty()) {
            // discontinued Scheme
            firstByAmfi = userSchemeDetailsEntityRepository
                    .findFirstByAmfi(schemeCode)
                    .orElseThrow(
                            () -> new SchemeNotFoundException("Fund with schemeCode " + schemeCode + " Not Found"));
            payOut = firstByAmfi.getIsin();
            persistSchemeInfo = true;
        } else {
            payOut = bySchemeId.get().getPayOut();
        }

        URI uri = UriComponentsBuilder.fromHttpUrl(historicalUrl).build().toUri();
        String oldSchemeId = null;
        try {
            String allNAVs = restClient.get().uri(uri).retrieve().body(String.class);
            Reader inputString = new StringReader(Objects.requireNonNull(allNAVs));
            try (BufferedReader br = new BufferedReader(inputString)) {
                String lineValue = br.readLine();
                for (int i = 0; i < 2; ++i) {
                    lineValue = br.readLine();
                }
                String schemeType = lineValue;
                String amc = lineValue;
                while (lineValue != null && !StringUtils.hasText(oldSchemeId)) {
                    String[] tokenize = lineValue.split(AppConstants.SEPARATOR);
                    boolean nonAmcRow = true;
                    boolean processRowByForce = false;
                    if (tokenize.length == 1) {
                        nonAmcRow = false;
                        String tempVal = lineValue;
                        lineValue = br.readLine();
                        if (!StringUtils.hasText(lineValue)) {
                            lineValue = br.readLine();
                            tokenize = lineValue.split(AppConstants.SEPARATOR);
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
                                MFSchemeEntity mfSchemeEntity =
                                        mfSchemeDtoToEntityMapper.mapMFSchemeDTOToMFSchemeEntity(mfSchemeDTO);
                                mfSchemeRepository.save(mfSchemeEntity);
                            }
                        }
                    }
                    lineValue = br.readLine();
                    if (!StringUtils.hasText(lineValue)) {
                        lineValue = br.readLine();
                    }
                }
            } catch (IOException e) {
                log.error("Exception Occurred while reading response", e);
                throw new NavNotFoundException("Unable to parse for %s".formatted(schemeCode), navDate);
            }
            if (!StringUtils.hasText(oldSchemeId) && firstByAmfi != null) {
                // Manually creating entry in mf_scheme table as no entry found in historical link
                MFSchemeEntity mfSchemeEntity = new MFSchemeEntity();
                mfSchemeEntity.setPayOut(payOut);
                mfSchemeEntity.setSchemeId(schemeCode);
                mfSchemeEntity.setSchemeName(firstByAmfi.getScheme());
                mfSchemeRepository.save(mfSchemeEntity);
                oldSchemeId = String.valueOf(schemeCode);
            } else {
                log.info("No Nav found for the given day");
            }
        } catch (ResourceAccessException exception) {
            // eating as we can't do much, and it should be set when available
            log.error("Unable to load Historical Data, downstream service is down ", exception);
        }
        return oldSchemeId;
    }
}
