/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.common;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.lifecycle.Startables;

public class AbstractTestContainerBase {

    @Container
    protected static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>(
                    "postgres:15.3-alpine")
            .withDatabaseName("integration-tests-db")
            .withUsername("username")
            .withPassword("password");

    protected static final GenericContainer LOKI_CONTAINER =
            new GenericContainer<>("grafana/loki").withExposedPorts(3100);

    static {
        Startables.deepStart(POSTGRE_SQL_CONTAINER, LOKI_CONTAINER).join();
    }

    @DynamicPropertySource
    static void setSqlContainer(DynamicPropertyRegistry propertyRegistry) {
        propertyRegistry.add("spring.datasource.url", POSTGRE_SQL_CONTAINER::getJdbcUrl);
        propertyRegistry.add("spring.datasource.username", POSTGRE_SQL_CONTAINER::getUsername);
        propertyRegistry.add("spring.datasource.password", POSTGRE_SQL_CONTAINER::getPassword);
        propertyRegistry.add(
                "application.loki.url",
                () -> "http://" + LOKI_CONTAINER.getHost() + ":" + LOKI_CONTAINER.getMappedPort(3100)
                        + "/loki/api/v1/push");
    }
}
