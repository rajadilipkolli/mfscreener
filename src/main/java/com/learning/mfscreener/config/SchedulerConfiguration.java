package com.learning.mfscreener.config;

import com.learning.mfscreener.service.SchemeService;
import org.jobrunr.scheduling.BackgroundJob;
import org.jobrunr.scheduling.cron.Cron;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SchedulerConfiguration {

    private final SchemeService schemeService;

    public SchedulerConfiguration(SchemeService schemeService) {
        this.schemeService = schemeService;
    }

    @EventListener(ApplicationStartedEvent.class)
    void setSchemeIfNotSetJob() {
        BackgroundJob.scheduleRecurrently(Cron.every5minutes(), schemeService::setAMFIIfNull);
    }
}
