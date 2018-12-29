package com.yyj.springbootscaffold.redis.lettuce;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by yyj on 2018/12/29.
 */
public interface IRedisRepository<T> {
    void set(RedisDatabaseConfig dbConfig, String key, T value, Integer ttl);

    void set(RedisDatabaseConfig dbConfig, String key, T value);

    Optional<T> get(RedisDatabaseConfig dbConfig, String key, Class clazz, Integer ttl);

    Optional<T> get(RedisDatabaseConfig dbConfig, String key, Class clazz);

    Optional<List<T>> mget(RedisDatabaseConfig dbConfig, List<String> keys, Class clazz);

    Optional<T> hget(RedisDatabaseConfig dbConfig, String key, String field, Class clazz);

    void hset(RedisDatabaseConfig dbConfig, String key, String field, T value, Integer ttl);

    void hmset(RedisDatabaseConfig dbConfig, String key, Map<String, T> fieldValues, Integer ttl);

    void hdel(RedisDatabaseConfig dbConfig, String key, String field);

    void clear(RedisDatabaseConfig dbConfig, String key);
}
