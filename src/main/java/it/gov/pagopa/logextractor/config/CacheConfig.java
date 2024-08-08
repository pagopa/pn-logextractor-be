package it.gov.pagopa.logextractor.config;

import java.net.URISyntaxException;
import java.time.Duration;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import it.gov.pagopa.logextractor.enums.RedisMode;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

import static it.gov.pagopa.logextractor.enums.RedisMode.SERVERLESS;

/**
 * Configuration class to manage the cache logics
 */
@Configuration
@Profile("!test")
@EnableCaching
@Slf4j
public class CacheConfig {

    @Value("${redis.hostname}")
    private String redisHostName;

    @Value("${redis.port}")
    private int redisPort;

    @Value("${redis.mode}")
    private RedisMode redisMode;

    @Value("${redis.user-id}")
    private String redisUserId;

    @Value("${redis.cache-name}")
    private String redisCacheName;

    @Value("${redis.cache-region}")
    private String redisCacheRegion;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() throws URISyntaxException {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHostName, redisPort);
        JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfiguration = JedisClientConfiguration.builder();

        if (redisMode == SERVERLESS) {
            jedisClientConfiguration.useSsl();
        } else {
            GenericObjectPoolConfig<Jedis> poolConfig = new JedisPoolConfig();
            poolConfig.setMaxIdle(30);
            poolConfig.setMinIdle(10);
            jedisClientConfiguration.usePooling().poolConfig(poolConfig);
        }

        return new PnLogExtractorConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration.build(), redisMode, redisUserId, redisCacheName, redisCacheRegion);
    }

    @Bean(value = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Primary
    @Bean(name = "cacheManager10Hour")
    public CacheManager cacheManager10Hour(RedisConnectionFactory redisConnectionFactory) {
        Duration expiration = Duration.ofHours(10);
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().entryTtl(expiration)).build();
    }
}

