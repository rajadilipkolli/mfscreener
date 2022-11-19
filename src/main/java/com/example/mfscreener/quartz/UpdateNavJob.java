/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.quartz;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@Slf4j
@DisallowConcurrentExecution
public class UpdateNavJob implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        log.info("Job Triggered at :{}", LocalDateTime.now());
    }
}
