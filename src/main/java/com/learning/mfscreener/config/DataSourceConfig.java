package com.learning.mfscreener.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration(proxyBeanMethods = false)
class DataSourceConfig {

    private final DataSourceProperties dataSourceProperties;

    DataSourceConfig(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    @Bean
    @Primary
    DataSource dataSource() {
        return createHikariDataSource("primary");
    }

    @Bean
    DataSource jobrunrDataSource() {
        return createHikariDataSource("jobrunr");
    }

    private DataSource createHikariDataSource(String poolName) {
        HikariDataSource hikariDataSource = dataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        hikariDataSource.setAutoCommit(false);
        hikariDataSource.setPoolName(poolName);
        return hikariDataSource;
    }
}
