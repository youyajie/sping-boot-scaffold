package com.yyj.springbootscaffold.redis.lettuce;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Created by yyj on 2018/12/29.
 */
public class RedisClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(RedisClientConfig.class);

    private static LettuceConnectionFactory redisConnectionFactory(RedisProperties redisProperties, Integer db) {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setDatabase(db != null ? db : redisProperties.getDatabase());
        redisStandaloneConfiguration.setHostName(redisProperties.getHost());
        redisStandaloneConfiguration.setPort(redisProperties.getPort());
        redisStandaloneConfiguration.setPassword(redisProperties.getPassword());


        LettuceClientConfiguration clientConfiguration = LettucePoolingClientConfiguration.builder()
                .poolConfig(genericObjectPoolConfig(redisProperties.getLettuce().getPool()))
                .build();

        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration,
                clientConfiguration);

        connectionFactory.afterPropertiesSet();
        return connectionFactory;
    }

    private static GenericObjectPoolConfig genericObjectPoolConfig(RedisProperties.Pool poolProperties) {
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        if(poolProperties == null)
            return genericObjectPoolConfig;

        genericObjectPoolConfig.setMaxIdle(poolProperties.getMaxIdle());
        genericObjectPoolConfig.setMinIdle(poolProperties.getMinIdle());
        genericObjectPoolConfig.setMaxTotal(poolProperties.getMaxActive());
        genericObjectPoolConfig.setMaxWaitMillis(poolProperties.getMaxWait().toMillis());
        genericObjectPoolConfig.setTestOnBorrow(true);
        genericObjectPoolConfig.setTestWhileIdle(true);
        return genericObjectPoolConfig;
    }

    public static RedisTemplate<String, Object> getRedisTemplate(RedisProperties redisProperties, Integer db) {
        RedisTemplate<String, Object> redisTemplate;

        try {
            redisTemplate = new RedisTemplate<>();
            redisTemplate.setConnectionFactory(redisConnectionFactory(redisProperties, db));
            setTemplateSerializer(redisTemplate);
//            redisTemplate.setEnableTransactionSupport(true);
        } catch (Exception e) {
            logger.error("redis: get redis template failed", e);
            throw e;
        }

        return redisTemplate;
    }

    private static void setTemplateSerializer(RedisTemplate redisTemplate) {
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        GenericFastJsonRedisSerializer fastJsonRedisSerializer = new GenericFastJsonRedisSerializer();

        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setDefaultSerializer(fastJsonRedisSerializer);
    }
}
