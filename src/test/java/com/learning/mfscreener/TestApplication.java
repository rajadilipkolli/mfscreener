package com.learning.mfscreener;

import com.learning.mfscreener.common.NonSQLContainersConfig;
import com.learning.mfscreener.common.SQLContainersConfig;
import org.springframework.boot.SpringApplication;

public class TestApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "test");
        SpringApplication.from(Application::main)
                .with(NonSQLContainersConfig.class, SQLContainersConfig.class)
                .run(args);
    }
}
