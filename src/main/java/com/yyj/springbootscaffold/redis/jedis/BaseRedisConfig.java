package com.yyj.springbootscaffold.redis.jedis;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by yyj on 2018/12/28.
 */
@Configuration
@EnableConfigurationProperties
public class BaseRedisConfig {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean
    @ConfigurationProperties("spring.redis")
    public RedisProperties getJedisRedisProperties() {
        return new RedisProperties();
    }

    /**
     * 默认连接,0库
     * @param redisProperties
     * @return
     */
    private JedisConnectionFactory getJedisConnectionFactory(RedisProperties redisProperties) {
        JedisConnectionFactory jedisConnectionFactory;

        try {
            //redis setting
            RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(
                    redisProperties.getHost(), redisProperties.getPort());
            redisStandaloneConfiguration.setDatabase(0);
            redisStandaloneConfiguration.setPassword(RedisPassword.of(redisProperties.getPassword()));
            jedisConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration);

            //jedis pool setting
            RedisProperties jedisProperties = getJedisRedisProperties();
            jedisConnectionFactory.getPoolConfig().setMaxIdle(jedisProperties.getJedis().getPool().getMaxIdle());
            jedisConnectionFactory.getPoolConfig().setMaxWaitMillis(
                    jedisProperties.getJedis().getPool().getMaxWait().toMillis());
        } catch (Exception e) {
            logger.error("redis: get jedis connect factory failed", e);
            throw e;
        }

        return jedisConnectionFactory;
    }

    private RedisTemplate<String, Object> getRedisTemplate(RedisProperties redisProperties) {
        RedisTemplate<String, Object> redisTemplate;

        try {
            redisTemplate = new RedisTemplate<>();
            redisTemplate.setConnectionFactory(getJedisConnectionFactory(redisProperties));
            redisTemplate.setDefaultSerializer(new GenericFastJsonRedisSerializer());
            redisTemplate.setEnableTransactionSupport(true);
        } catch (Exception e) {
            logger.error("redis: get redis template failed", e);
            throw e;
        }

        return redisTemplate;
    }

    @Bean
    @ConfigurationProperties("spring.redis.yyj")
    @Primary
    public RedisProperties getCrmRedisProperties() {
        return new RedisProperties();
    }

    @Bean(name = "redisTemplate")
    public RedisTemplate<String, Object> getCrmRedisTemplate() {
        return getRedisTemplate(getCrmRedisProperties());
    }

}

