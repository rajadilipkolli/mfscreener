package com.example.mfscreener.quartz;

import com.example.mfscreener.quartz.job.UpdateNavJob;
import org.quartz.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "quartz", name = "enable", havingValue = "true")
public class QuartzConfiguration {

    public static final String UPDATE_NAV_GROUP = "UpdateNAV";

    @Bean
    public JobDetail createJobNAVUpdate() {
        return JobBuilder.newJob(UpdateNavJob.class)
                .withIdentity(new JobKey(UPDATE_NAV_GROUP + "-JOB", UPDATE_NAV_GROUP))
                .storeDurably()
                .requestRecovery()
                .build();
    }

    @Bean
    public Trigger triggerNavUpdate() {
        return TriggerBuilder.newTrigger()
                .forJob(createJobNAVUpdate())
                .withIdentity(UPDATE_NAV_GROUP + "-TRIGGER", UPDATE_NAV_GROUP)
                .withSchedule(
                        SimpleScheduleBuilder.repeatHourlyForever()
                                .withMisfireHandlingInstructionIgnoreMisfires())
                .build();
    }
}
