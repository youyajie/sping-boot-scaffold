package com.yyj.springbootscaffold.redis.lettuce;

import com.yyj.springbootscaffold.redis.RedisDatabaseEnum;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by yyj on 2018/12/29.
 */
@Configuration
public class BaseRedisConfig {
    @Bean
    @ConfigurationProperties("spring.redis.yyj")
    @Primary
    public RedisProperties getYyjRedisProperties() {
        return new RedisProperties();
    }

    @Bean(name = "yyjZeroRedisTemplate")
    public RedisTemplate<String, Object> getYyjZeroRedisTemplate() {
        return RedisClientConfig.getRedisTemplate(getYyjRedisProperties(), null);
    }

    @Bean(name = "yyjOneRedisTemplate")
    public RedisTemplate<String, Object> getYyjOneRedisTemplate() {
        return RedisClientConfig.getRedisTemplate(getYyjRedisProperties(), RedisDatabaseEnum.FIRST.getDatabaseId());
    }

    //同上可配置yyj缓存的其他库
    //整体同上可配置其他 Redis 缓存
}
