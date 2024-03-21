package com.learning.mfscreener.config;

import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.mapper.MfSchemeDtoToEntityMapper;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.repository.MFSchemeRepository;
import com.learning.mfscreener.repository.MFSchemeTypeRepository;
import com.learning.mfscreener.utils.AppConstants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class Initializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Initializer.class);

    private final MFSchemeRepository mfSchemesRepository;
    private final MFSchemeTypeRepository mfSchemeTypeRepository;
    private final MfSchemeDtoToEntityMapper mfSchemeDtoToEntityMapper;
    private final RestTemplate restTemplate;

    public Initializer(
            MFSchemeRepository mfSchemesRepository,
            MFSchemeTypeRepository mfSchemeTypeRepository,
            MfSchemeDtoToEntityMapper mfSchemeDtoToEntityMapper,
            RestTemplate restTemplate) {
        this.mfSchemesRepository = mfSchemesRepository;
        this.mfSchemeTypeRepository = mfSchemeTypeRepository;
        this.mfSchemeDtoToEntityMapper = mfSchemeDtoToEntityMapper;
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(String... args) throws IOException {
        long start = System.currentTimeMillis();
        log.info("Loading All Funds...");
        try {
            String allNAVs = restTemplate.getForObject(AppConstants.AMFI_WEBSITE_LINK, String.class);
            Reader inputString = new StringReader(Objects.requireNonNull(allNAVs));
            List<MFSchemeDTO> chopArrayList = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(inputString)) {
                String lineValue = br.readLine();
                for (int i = 0; i < 2; ++i) {
                    lineValue = br.readLine();
                }
                String schemeType = lineValue;
                String amc = lineValue;
                while (lineValue != null) {
                    boolean nonAmcRow = true;
                    String[] tokenize = lineValue.split(AppConstants.NAV_SEPARATOR);
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
                        final String payout = tokenize[1];
                        final String reinvestment = tokenize[2];
                        final String schemename = tokenize[3];
                        final String nav = tokenize[4];
                        final String date = tokenize[5];
                        final MFSchemeDTO tempObj = new MFSchemeDTO(
                                amc, Long.valueOf(schemecode), payout, schemename, nav, date, schemeType);
                        chopArrayList.add(tempObj);
                    }
                    lineValue = br.readLine();
                    if (!StringUtils.hasText(lineValue)) {
                        lineValue = br.readLine();
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
                chopArrayList.removeIf(s -> schemeCodesList.contains(s.schemeCode()));
                chopArrayList.forEach(scheme -> {
                    MFSchemeEntity mfSchemeEntity =
                            mfSchemeDtoToEntityMapper.mapMFSchemeDTOToMFSchemeEntity(scheme, mfSchemeTypeRepository);
                    list.add(mfSchemeEntity);
                });
                mfSchemesRepository.saveAll(list);
                stopWatch.stop();
                log.info("saved in db in : {} sec", stopWatch.getTotalTimeSeconds());
            }
        } catch (HttpClientErrorException | ResourceAccessException httpClientErrorException) {
            // eating as we can't do much, it should be set when available using Nightly job
            log.error("Unable to load data from :{}", AppConstants.AMFI_WEBSITE_LINK, httpClientErrorException);
        }
    }
}
