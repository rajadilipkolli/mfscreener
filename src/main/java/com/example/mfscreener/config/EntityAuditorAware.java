package com.example.mfscreener.config;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;

public class EntityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of("Raja");
    }
}
