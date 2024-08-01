package com.learning.mfscreener.config;

import com.learning.mfscreener.utils.AppConstants;
import io.lettuce.core.RedisClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration(proxyBeanMethods = false)
@Profile(AppConstants.PROFILE_NOT_LOCAL)
class RedisConfig {

    @Bean
    RedisClient redisClient(LettuceConnectionFactory lettuceConnectionFactory) {
        return (RedisClient) lettuceConnectionFactory.getNativeClient();
    }
}
