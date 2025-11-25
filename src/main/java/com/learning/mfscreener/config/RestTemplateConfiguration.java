package com.learning.mfscreener.config;

import java.time.Duration;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration(proxyBeanMethods = false)
@EnableCaching
public class RestTemplateConfiguration {

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    RestClient restClient(RestTemplate restTemplate) {
        return RestClient.create(restTemplate);
    }
}
