package com.learning.mfscreener;

import com.learning.mfscreener.common.NonSQLContainersConfig;
import com.learning.mfscreener.common.SQLContainersConfig;
import com.learning.mfscreener.utils.AppConstants;
import org.springframework.boot.SpringApplication;

public class TestApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", AppConstants.PROFILE_TEST);
        SpringApplication.from(Application::main)
                .with(NonSQLContainersConfig.class, SQLContainersConfig.class)
                .run(args);
    }
}
