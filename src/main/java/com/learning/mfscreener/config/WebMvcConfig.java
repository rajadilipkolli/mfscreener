package com.learning.mfscreener.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@EnableCaching
public class WebMvcConfig implements WebMvcConfigurer {
    private final ApplicationProperties applicationProperties;

    public WebMvcConfig(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        ApplicationProperties.Cors propertiesCors = applicationProperties.getCors();
        registry.addMapping(propertiesCors.getPathPattern())
                .allowedMethods(propertiesCors.getAllowedMethods())
                .allowedHeaders(propertiesCors.getAllowedHeaders())
                .allowedOriginPatterns(propertiesCors.getAllowedOriginPatterns())
                .allowCredentials(propertiesCors.isAllowCredentials());
    }
}
