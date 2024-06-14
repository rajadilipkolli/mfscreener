package com.learning.mfscreener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestApplication {

    @Bean
    @ServiceConnection(name = "redis")
    @RestartScope
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis").withTag("7.2.4-alpine")).withExposedPorts(6379);
    }

    @Bean
    @ServiceConnection
    @RestartScope
    PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres").withTag("16-alpine"));
    }

    @Bean
    @ServiceConnection(name = "openzipkin/zipkin")
    @RestartScope
    GenericContainer<?> zipkinContainer() {
        return new GenericContainer<>("openzipkin/zipkin:latest").withExposedPorts(9411);
    }

    public static void main(String[] args) {
        SpringApplication.from(Application::main).with(TestApplication.class).run(args);
    }
}
