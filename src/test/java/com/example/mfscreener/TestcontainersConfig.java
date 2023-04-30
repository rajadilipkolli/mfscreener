/* Licensed under Apache-2.0 2023. */
package com.example.mfscreener;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>("postgres:15.2-alpine");
    }

    static final GenericContainer LOKI_CONTAINER =
            new GenericContainer(DockerImageName.parse("grafana/loki")).withExposedPorts(3100);

    static {
        LOKI_CONTAINER.start();
        System.setProperty(
                "application.loki.url",
                "http://" + LOKI_CONTAINER.getHost() + ":" + LOKI_CONTAINER.getMappedPort(3100) + "/loki/api/v1/push");
    }
}
