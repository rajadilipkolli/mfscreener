package com.example.mfscreener.quartz.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.time.LocalDateTime;

@Slf4j
@DisallowConcurrentExecution
public class UpdateNavJob implements Job {

  @Override
  public void execute(JobExecutionContext context) {
    log.info("Job Triggered at :{}", LocalDateTime.now());
  }
}
