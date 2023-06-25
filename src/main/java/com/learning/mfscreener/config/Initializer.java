package com.learning.mfscreener.config;

import com.learning.mfscreener.adapter.ConversionServiceAdapter;
import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.repository.MFSchemeRepository;
import com.learning.mfscreener.utils.AppConstants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class Initializer implements CommandLineRunner {

    private final MFSchemeRepository mfSchemesRepository;
    private final ConversionServiceAdapter conversionServiceAdapter;
    private final RestTemplate restTemplate;

    @Override
    public void run(String... args) throws IOException {
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
                    final MFSchemeDTO tempObj = new MFSchemeDTO(schemecode, payout, schemename, nav, date);
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
            List<MFSchemeEntity> list = new ArrayList<>();
            List<Long> schemeCodesList = mfSchemesRepository.findAllSchemeIds();
            chopArrayList.removeIf(s -> schemeCodesList.contains(Long.valueOf(s.schemeCode())));
            chopArrayList.forEach(scheme -> {
                MFSchemeEntity mfSchemeEntity = conversionServiceAdapter.mapMFSchemeDTOToMFSchemeEntity(scheme);
                list.add(mfSchemeEntity);
            });
            mfSchemesRepository.saveAll(list);
            stopWatch.stop();
            log.info("saved in db in : {} sec", stopWatch.getTotalTimeSeconds());
        }
    }
}
