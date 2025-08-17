package com.learning.mfscreener.common;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class NonSQLContainersConfig {

    @Bean
    @ServiceConnection(name = "redis")
    @RestartScope
    RedisContainer redisContainer() {
        return new RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME.withTag("8.2.0-alpine"));
    }
}
