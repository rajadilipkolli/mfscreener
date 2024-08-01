package com.learning.mfscreener;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestApplication {

    @Bean
    @ServiceConnection(name = "redis")
    @RestartScope
    RedisContainer redisContainer() {
        return new RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME.withTag("7.2.5-alpine"));
    }

    @Bean
    @ServiceConnection
    @RestartScope
    PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres").withTag("16-alpine"));
    }

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "test");
        SpringApplication.from(Application::main).with(TestApplication.class).run(args);
    }
}
