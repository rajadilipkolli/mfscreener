/* Licensed under Apache-2.0 2021-2024. */
package com.learning.mfscreener.config.audit;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;

public class EntityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of("Application");
    }
}
