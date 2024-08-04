package com.learning.mfscreener.config.db;

import com.learning.mfscreener.utils.AppConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@ConditionalOnMissingBean(JdbcConnectionDetails.class)
@Configuration(proxyBeanMethods = false)
@Profile(AppConstants.PROFILE_NOT_TEST)
class JdbcConnectionDetailsConfig {

    @Bean
    CustomJdbcConnectionDetails jdbcConnectionDetails(DataSourceProperties properties) {
        return new CustomJdbcConnectionDetails(properties);
    }

    private record CustomJdbcConnectionDetails(DataSourceProperties properties) implements JdbcConnectionDetails {

        @Override
        public String getUsername() {
            return this.properties.determineUsername();
        }

        @Override
        public String getPassword() {
            return this.properties.determinePassword();
        }

        @Override
        public String getJdbcUrl() {
            return this.properties.determineUrl();
        }

        @Override
        public String getDriverClassName() {
            return this.properties.determineDriverClassName();
        }

        @Override
        public String getXaDataSourceClassName() {
            return (this.properties.getXa().getDataSourceClassName() != null)
                    ? this.properties.getXa().getDataSourceClassName()
                    : JdbcConnectionDetails.super.getXaDataSourceClassName();
        }
    }
}
