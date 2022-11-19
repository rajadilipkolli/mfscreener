/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.quartz;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "quartz", name = "enable", havingValue = "true")
public class QuartzConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "dataSource")
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    @Bean(name = "quartzDataSource")
    @QuartzDataSource
    @DependsOn("hikariConfig")
    public DataSource quartzDataSource(HikariConfig hikariConfig) {
        return new HikariDataSource(hikariConfig);
    }

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
