package com.example.mfscreener.bootstrap;

import com.example.mfscreener.convertor.NavServiceConvertor;
import com.example.mfscreener.entities.MFScheme;
import com.example.mfscreener.exception.NavNotFoundException;
import com.example.mfscreener.exception.SchemeNotFoundException;
import com.example.mfscreener.model.Scheme;
import com.example.mfscreener.repository.MFSchemeRepository;
import com.example.mfscreener.service.NavService;
import com.example.mfscreener.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoadInitialData {

    private final MFSchemeRepository mfSchemesRepository;
    private final NavServiceConvertor navServiceConvertor;
    private final NavService navService;

    @EventListener(value = ApplicationStartedEvent.class)
    void loadAllFunds() throws IOException {
        long start = System.currentTimeMillis();
        log.info("Loading All Funds...");
        URL url = new URL(Constants.AMFI_WEBSITE_LINK);
        List<Scheme> chopArrayList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String fileRead = br.readLine();
            for (int i = 0; i < 4; ++i) {
                fileRead = br.readLine();
            }
            while (fileRead != null) {
                int check = 0;
                final String[] tokenize = fileRead.split(Constants.SEPARATOR);
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
                    final Scheme tempObj = new Scheme(schemecode, payout, schemename, nav, date);
                    chopArrayList.add(tempObj);
                }
                fileRead = br.readLine();
                if ("".equals(fileRead)) {
                    fileRead = br.readLine();
                }
            }
        }
        log.info("All Funds loaded in {} milliseconds, total funds loaded :{}", (System.currentTimeMillis() - start), chopArrayList.size());

        if (mfSchemesRepository.count() != chopArrayList.size()) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("saving fundNames");
            List<MFScheme> list = new ArrayList<>();
            chopArrayList.forEach(scheme -> {
                MFScheme mfSchemeEntity = navServiceConvertor.convert(scheme);
                list.add(mfSchemeEntity);
            });
            mfSchemesRepository.saveAll(list);
            stopWatch.stop();
            log.info("saved in db in : {} sec", stopWatch.getTotalTimeSeconds());
            stopWatch.start("loadDetails");
            for (MFScheme mfScheme: list) {
                try {
                    navService.fetchSchemeDetails(mfScheme.getSchemeId());
                } catch (SchemeNotFoundException | NavNotFoundException exception) {
                    log.error(exception.getMessage());
                }
            }
            stopWatch.stop();
            log.info("Fund House and Scheme Type Set in : {} sec", stopWatch.getTotalTimeSeconds());
        }
    }
}
