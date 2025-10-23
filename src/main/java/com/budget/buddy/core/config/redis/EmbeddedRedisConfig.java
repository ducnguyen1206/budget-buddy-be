package com.budget.buddy.core.config.redis;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import java.io.IOException;

@Configuration
public class EmbeddedRedisConfig {
    private RedisServer redisServer;

    @Bean
    public RedisServer redisServer() throws IOException {
        redisServer = new RedisServer(6379);
        redisServer.start();
        return redisServer;
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
        }
    }
}
