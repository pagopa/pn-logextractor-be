package it.gov.pagopa.logextractor.config;

import java.time.Duration;

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
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;

/**
 * Configuration class to manage the cache logics*/
@Configuration
@Profile("!test")
@EnableCaching
public class CacheConfig {
	
	@Value("${redis.hostname}")
    private String redisHostName;
 
    @Value("${redis.port}")
    private int redisPort;
 
    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHostName, redisPort);
        JedisConnectionFactory conf = new JedisConnectionFactory(redisStandaloneConfiguration);
        /*GenericObjectPoolConfig<Jedis> poolConfig = conf.getPoolConfig();
        if(poolConfig != null) {
            poolConfig.setMaxIdle(30);
            poolConfig.setMinIdle(10);
        }*/
        conf.setUsePool(false);
        return conf;
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

