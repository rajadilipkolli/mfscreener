package com.learning.mfscreener.config;

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
                .allowedMethods(propertiesCors.getAllowedMethods().split(","))
                .allowedHeaders(propertiesCors.getAllowedHeaders().split(","))
                .allowedOriginPatterns(propertiesCors.getAllowedOriginPatterns().split(","))
                .allowCredentials(propertiesCors.isAllowCredentials());
    }
}
