package com.learning.mfscreener.config;

import io.lettuce.core.RedisClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration(proxyBeanMethods = false)
public class RedisConfig {

    @Bean
    RedisClient redisClient(LettuceConnectionFactory lettuceConnectionFactory) {
        return (RedisClient) lettuceConnectionFactory.getNativeClient();
    }
}
