/* Licensed under Apache-2.0 2021-2024. */
package com.learning.mfscreener.config.audit;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing
public class AuditConfiguration {

    @Bean
    AuditorAware<String> auditorAware() {
        return () -> Optional.of("App");
    }
}
