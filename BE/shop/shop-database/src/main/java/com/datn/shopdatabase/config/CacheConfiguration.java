package com.datn.shopdatabase.config;

import com.datn.shopdatabase.config.properties.CacheProperties;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.net.URI;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfiguration {

    @Autowired(required = false)
    private GitProperties gitProperties;

    @Autowired(required = false)
    private BuildProperties buildProperties;

    @Bean
    public javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration(CacheProperties properties) {
        MutableConfiguration<Object, Object> jcacheConfig = new MutableConfiguration<>();

        CacheProperties.Redis redis = properties.getRedis();
        URI redisUri = URI.create(redis.getServer()[0]);
        Config config = new Config();

        if (redis.isCluster()) {
            var clusterServers = config.useClusterServers()
                    .setMasterConnectionPoolSize(redis.getConnectionPoolSize())
                    .setMasterConnectionMinimumIdleSize(redis.getConnectionMinimumIdleSize())
                    .setSubscriptionConnectionPoolSize(redis.getSubscriptionConnectionPoolSize())
                    .setRetryAttempts(3)
                    .addNodeAddress(redis.getServer());
            if (redisUri.getUserInfo() != null) {
                clusterServers.setPassword(redisUri.getUserInfo().split(":",2)[1]);
            }
        } else {
            var singleServer = config.useSingleServer()
                    .setAddress(redis.getServer()[0])
                    .setConnectionPoolSize(redis.getConnectionPoolSize())
                    .setConnectionMinimumIdleSize(redis.getConnectionMinimumIdleSize())
                    .setSubscriptionConnectionPoolSize(redis.getSubscriptionConnectionPoolSize())
                    .setRetryAttempts(3);
            if (redisUri.getUserInfo() != null) {
                singleServer.setPassword(redisUri.getUserInfo().split(":",2)[1]);
            }
        }

        jcacheConfig.setStatisticsEnabled(true);
        jcacheConfig.setExpiryPolicyFactory(
                CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, redis.getExpiration()))
        );

        return RedissonConfiguration.fromInstance(Redisson.create(config), jcacheConfig);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer(
            javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration) {

        return cm -> {
            createCache(cm, "cartCache", jcacheConfiguration);
        };
    }


    private void createCache(
            javax.cache.CacheManager cm,
            String cacheName,
            javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration
    ) {
        var cache = cm.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        } else {
            cm.createCache(cacheName, jcacheConfiguration);
        }
    }


    @Bean
    public KeyGenerator keyGenerator() {
        return new PrefixedKeyGenerator(this.gitProperties, this.buildProperties);
    }
}
