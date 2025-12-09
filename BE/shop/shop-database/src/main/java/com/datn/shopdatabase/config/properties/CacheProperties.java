package com.datn.shopdatabase.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cache", ignoreUnknownFields = false)
public class CacheProperties {

    private final Redis redis = new Redis();

    @Data
    public static class Redis {
        private String[] server = YubutuDefault.Redis.server;
        private int expiration = YubutuDefault.Redis.expiration;
        private boolean cluster = YubutuDefault.Redis.cluster;
        private int connectionPoolSize = YubutuDefault.Redis.connectionPoolSize;
        private int connectionMinimumIdleSize = YubutuDefault.Redis.connectionMinimumIdleSize;
        private int subscriptionConnectionPoolSize = YubutuDefault.Redis.subscriptionConnectionPoolSize;
        private int subscriptionConnectionMinimumIdleSize = YubutuDefault.Redis.subscriptionConnectionMinimumIdleSize;
    }
}
