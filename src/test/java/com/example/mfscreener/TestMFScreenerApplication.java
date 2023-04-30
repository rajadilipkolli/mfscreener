package com.example.mfscreener;

import org.springframework.boot.SpringApplication;

public class TestMFScreenerApplication {

    public static void main(String[] args) {
        SpringApplication.from(MFScreenerApplication::main)
                .with(TestcontainersConfig.class)
                .run(args);
    }
}
