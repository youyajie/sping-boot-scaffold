package com.yyj.springbootscaffold.redis.jedis;

import com.yyj.springbootscaffold.redis.RedisDatabaseEnum;

import java.util.List;
import java.util.Map;

/**
 * Created by yyj on 2018/12/28.
 */
public interface IRedisDao {

    void put(RedisDatabaseEnum db, String key, Object value, Integer ttl);

    void put(RedisDatabaseEnum db, String key, Object value);

    <T> T get(RedisDatabaseEnum db, String key, Class clazz, Integer ttl);

    <T> T get(RedisDatabaseEnum db, String key, Class clazz);

    String hget(RedisDatabaseEnum db, String key, String field);

    void hmset(RedisDatabaseEnum db, String key, Map<String, String> fieldValues, Integer ttl);

    List<String> mget(RedisDatabaseEnum db, List<String> keys);

    Boolean sismember(RedisDatabaseEnum db, String key,Object value);

    void srem(RedisDatabaseEnum db,String key,Object[] values);

    void sadd(RedisDatabaseEnum db,String key,Object[] values);

    Integer clear(RedisDatabaseEnum db, String regex);

}
