package com.learning.mfscreener.config;

import com.learning.mfscreener.service.HistoricalNavService;
import com.learning.mfscreener.service.SchemeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Initializer implements CommandLineRunner {

    private final SchemeService schemeService;
    private final HistoricalNavService historicalNavService;

    public Initializer(SchemeService schemeService, HistoricalNavService historicalNavService) {
        this.schemeService = schemeService;
        this.historicalNavService = historicalNavService;
    }

    @Override
    public void run(String... args) {
        if (!schemeService.navLoadedFor31Jan2018()) {
            historicalNavService.getHistoricalNavOn31Jan2018();
        }
    }
}
