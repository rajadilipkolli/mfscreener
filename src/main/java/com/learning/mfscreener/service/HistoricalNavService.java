package com.learning.mfscreener.service;

import static com.learning.mfscreener.utils.AppConstants.FORMATTER_DD_MMM_YYYY;

import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.exception.NavNotFoundException;
import com.learning.mfscreener.repository.MFSchemeRepository;
import com.learning.mfscreener.utils.AppConstants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.time.LocalDate;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class HistoricalNavService {

    private final MFSchemeRepository mfSchemeRepository;
    private final RestClient restClient;

    private static final Logger log = LoggerFactory.getLogger(HistoricalNavService.class);

    public HistoricalNavService(MFSchemeRepository mfSchemeRepository, RestClient restClient) {
        this.mfSchemeRepository = mfSchemeRepository;
        this.restClient = restClient;
    }

    public String getHistoricalNav(Long schemeCode, LocalDate navDate) {
        // URL https://portal.amfiindia.com/DownloadNAVHistoryReport_Po.aspx?tp=1&frmdt=01-Jan-2024&todt=03-Jan-2024
        String toDate = navDate.format(FORMATTER_DD_MMM_YYYY);
        String fromDate = navDate.minusDays(3).format(FORMATTER_DD_MMM_YYYY);
        String historicalUrl = "https://portal.amfiindia.com/DownloadNAVHistoryReport_Po.aspx?tp=1&frmdt=%s&todt=%s"
                .formatted(fromDate, toDate);
        MFSchemeEntity bySchemeId =
                mfSchemeRepository.findBySchemeId(schemeCode).orElse(null);
        String payOut = bySchemeId.getPayOut();
        URI uri = UriComponentsBuilder.fromHttpUrl(historicalUrl).build().toUri();
        String allNAVs = restClient.get().uri(uri).retrieve().body(String.class);
        Reader inputString = new StringReader(Objects.requireNonNull(allNAVs));
        String oldSchemeId = null;
        try (BufferedReader br = new BufferedReader(inputString)) {
            String lineValue = br.readLine();
            for (int i = 0; i < 4; ++i) {
                lineValue = br.readLine();
            }
            while (lineValue != null && !StringUtils.hasText(oldSchemeId)) {
                int check = 0;
                final String[] tokenize = lineValue.split(AppConstants.SEPARATOR);
                if (tokenize.length == 1) {
                    check = 1;
                }
                if (check == 0) {
                    final String schemecode = tokenize[0];
                    final String payout = tokenize[2];
                    if (payOut.equalsIgnoreCase(payout)) {
                        oldSchemeId = schemecode;
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
        return oldSchemeId;
    }
}
