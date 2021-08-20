package com.example.mfscreener;

import com.example.mfscreener.service.NavService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class MFScreenerApplication implements CommandLineRunner {

    @Autowired
    private NavService navService;

    public static void main(String[] args) {
        SpringApplication.run(MFScreenerApplication.class);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @Override
    public void run(String... args) throws Exception {
        navService.loadNavForAllFunds();
    }
}
