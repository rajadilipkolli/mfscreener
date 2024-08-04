package com.learning.mfscreener.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration(proxyBeanMethods = false)
class DBConfiguration {

    private final DataSourceProperties dataSourceProperties;
    private final JdbcConnectionDetails jdbcConnectionDetails;

    DBConfiguration(DataSourceProperties dataSourceProperties, JdbcConnectionDetails jdbcConnectionDetails) {
        this.dataSourceProperties = dataSourceProperties;
        this.jdbcConnectionDetails = jdbcConnectionDetails;
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
                .password(jdbcConnectionDetails.getPassword())
                .username(jdbcConnectionDetails.getUsername())
                .url(jdbcConnectionDetails.getJdbcUrl())
                .driverClassName(jdbcConnectionDetails.getDriverClassName())
                .build();
        hikariDataSource.setAutoCommit(false);
        hikariDataSource.setPoolName(poolName);
        return hikariDataSource;
    }
}
