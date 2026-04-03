package com.learning.mfscreener.config;

import java.util.Arrays;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
class WebMvcConfig implements WebMvcConfigurer {
    private final ApplicationProperties applicationProperties;

    WebMvcConfig(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        ApplicationProperties.Cors propertiesCors = applicationProperties.getCors();
        registry.addMapping(propertiesCors.getPathPattern())
                .allowedMethods(Arrays.stream(propertiesCors.getAllowedMethods().split(","))
                        .map(String::trim)
                        .toArray(String[]::new))
                .allowedHeaders(Arrays.stream(propertiesCors.getAllowedHeaders().split(","))
                        .map(String::trim)
                        .toArray(String[]::new))
                .allowedOriginPatterns(
                        Arrays.stream(propertiesCors.getAllowedOriginPatterns().split(","))
                                .map(String::trim)
                                .toArray(String[]::new))
                .allowCredentials(propertiesCors.isAllowCredentials());
    }
}
