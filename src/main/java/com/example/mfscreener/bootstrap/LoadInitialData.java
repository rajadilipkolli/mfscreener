/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.bootstrap;

import com.example.mfscreener.adapter.ConversionServiceAdapter;
import com.example.mfscreener.entities.MFScheme;
import com.example.mfscreener.models.MFSchemeDTO;
import com.example.mfscreener.repository.MFSchemeRepository;
import com.example.mfscreener.utils.AppConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
@Profile(AppConstants.PROFILE_NOT_TEST)
public class LoadInitialData {

    private static final String COMMA_DELIMITER = ",";
    private final MFSchemeRepository mfSchemesRepository;
    private final ConversionServiceAdapter conversionServiceAdapter;
    private final RestTemplate restTemplate;

    @EventListener(value = ApplicationStartedEvent.class)
    void loadAllFunds() throws IOException {
        long start = System.currentTimeMillis();
        log.info("Loading All Funds...");
        List<MFSchemeDTO> chopArrayList = new ArrayList<>();
        String allNAVs = restTemplate.getForObject(AppConstants.AMFI_WEBSITE_LINK, String.class);
        Reader inputString = new StringReader(Objects.requireNonNull(allNAVs));
        try (BufferedReader br = new BufferedReader(inputString)) {
            String fileRead = br.readLine();
            for (int i = 0; i < 4; ++i) {
                fileRead = br.readLine();
            }
            while (fileRead != null) {
                int check = 0;
                final String[] tokenize = fileRead.split(AppConstants.SEPARATOR);
                if (tokenize.length == 1) {
                    check = 1;
                }
                if (check == 0) {
                    final String schemecode = tokenize[0];
                    final String payout = tokenize[1];
                    final String reinvestment = tokenize[2];
                    final String schemename = tokenize[3];
                    final String nav = tokenize[4];
                    final String date = tokenize[5];
                    final MFSchemeDTO tempObj =
                            new MFSchemeDTO(schemecode, payout, schemename, nav, date);
                    chopArrayList.add(tempObj);
                }
                fileRead = br.readLine();
                if ("".equals(fileRead)) {
                    fileRead = br.readLine();
                }
            }
        }
        log.info(
                "All Funds loaded in {} milliseconds, total funds loaded :{}",
                (System.currentTimeMillis() - start),
                chopArrayList.size());

        if (mfSchemesRepository.count() != chopArrayList.size()) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("saving fundNames");
            List<MFScheme> list = new ArrayList<>();
            List<Long> schemeCodesList = mfSchemesRepository.findAllSchemeIds();
            chopArrayList.removeIf(s -> schemeCodesList.contains(Long.valueOf(s.schemeCode())));
            chopArrayList.forEach(
                    scheme -> {
                        MFScheme mfSchemeEntity =
                                conversionServiceAdapter.mapMFSchemeDTOToMFScheme(scheme);
                        list.add(mfSchemeEntity);
                    });
            mfSchemesRepository.saveAll(list);
            stopWatch.stop();
            loadAlias();
            log.info("saved in db in : {} sec", stopWatch.getTotalTimeSeconds());
        }
    }

    private void loadAlias() {
        Map<String, Long> records = new HashMap<>();
        try {
            File file = ResourceUtils.getFile("classpath:scheme-mapping.csv");
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(COMMA_DELIMITER);
                    String schemeName = values[0].substring(1, values[0].length() - 1);
                    if (!(schemeName.equals("schemaname") || "NULL".equals(values[1]))) {
                        records.put(schemeName, Long.parseLong(values[1]));
                    }
                }
            }
        } catch (IOException e) {
            log.error("IOException", e);
        }
        records.forEach(mfSchemesRepository::updateSchemeNameAliasBySchemeId);
    }
}
