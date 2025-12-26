package com.learning.mfscreener.common;

import com.redis.testcontainers.RedisContainer;
import java.time.Duration;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class NonSQLContainersConfig {

    @Bean
    @ServiceConnection(name = "redis")
    @RestartScope
    RedisContainer redisContainer() {
        return new RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME.withTag("8.4.0-alpine"));
    }

    @Bean
    @ServiceConnection
    @RestartScope
    LgtmStackContainer lgtmContainer() {
        return new LgtmStackContainer(DockerImageName.parse("grafana/otel-lgtm:0.13.0"))
                .withStartupTimeout(Duration.ofMinutes(2));
    }
}
