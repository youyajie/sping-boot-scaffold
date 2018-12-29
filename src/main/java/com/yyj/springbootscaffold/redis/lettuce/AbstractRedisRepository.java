package com.yyj.springbootscaffold.redis.lettuce;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by yyj on 2018/12/29.
 */
public abstract class AbstractRedisRepository<T> implements IRedisRepository<T>{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private StringRedisSerializer stringRedisSerializer;
    private GenericFastJsonRedisSerializer fastJsonRedisSerializer;

    {
        stringRedisSerializer = new StringRedisSerializer();
        fastJsonRedisSerializer = new GenericFastJsonRedisSerializer();
    }

    @Override
    public void set(RedisDatabaseConfig dbConfig, String key, T value, Integer ttl) {
        setInvoke(dbConfig, key, value, ttl);
    }

    @Override
    public void set(RedisDatabaseConfig dbConfig, String key, T value) {
        setInvoke(dbConfig, key, value, null);
    }

    /**
     * @param key 查询的key
     * @param clazz 返回值的Class
     * @param ttl 过期时间
     * @return
     */
    @Override
    public Optional<T> get(RedisDatabaseConfig dbConfig, String key, Class clazz, Integer ttl) {
        return getInvoke(dbConfig, key, clazz, ttl);
    }

    /**
     *
     * @param key 查询的key
     * @param clazz 返回值的Class
     * @return
     */
    @Override
    public Optional<T> get(RedisDatabaseConfig dbConfig, String key, Class clazz) {
        return getInvoke(dbConfig, key, clazz, null);
    }

    @Override
    public Optional<List<T>> mget(RedisDatabaseConfig dbConfig, List<String> keys, Class clazz) {
        if (CollectionUtils.isEmpty(keys)) {
            logger.warn("参数不合法：key={}", keys);
        }

        Optional<List<T>> re = Optional.empty();
        try {
            RedisTemplate<String, Object> redisTemplate = getRedisTemplate(dbConfig);
            adaptSerializer(redisTemplate, clazz, null);

            List<Object> reObj = redisTemplate.opsForValue().multiGet(keys);

            if(!CollectionUtils.isEmpty(reObj)) {
                re = Optional.ofNullable(reObj.stream()
                        .filter(item -> item != null)
                        .map(item -> (T)clazz.cast(item))
                        .collect(Collectors.toList()));
            }
        } catch (Exception e) {
            logger.warn("mget redis失败,key={}", keys);
        }

        return re;
    }

    @Override
    public Optional<T> hget(RedisDatabaseConfig dbConfig, String key, String field, Class clazz) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
            logger.warn("参数不合法：key={},field={}", key, field);
        }

        Optional<T> result = Optional.empty();
        try {
            RedisTemplate<String, Object> redisTemplate = getRedisTemplate(dbConfig);
            adaptSerializer(redisTemplate, clazz, null);

            Object obj = redisTemplate.opsForHash().get(key, field);

            result = Optional.ofNullable((T)clazz.cast(obj));
        } catch (Exception e) {
            logger.warn("hget redis failed,key={}", key, e);
        }

        return result;
    }

    @Override
    public void hset(RedisDatabaseConfig dbConfig, String key, String field, T value, Integer ttl) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
            logger.warn("参数不合法：key={},field={}", key, field);
        }

        try {
            RedisTemplate<String, Object> redisTemplate = getRedisTemplate(dbConfig);
            adaptSerializer(redisTemplate, null, value);

            redisTemplate.opsForHash().put(key, field, value);

            if(ttl != null && ttl > 0)
                redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("hset redis失败,key={},field={},value={}", key, field,
                    (value != null && JSON.toJSONString(value).length() > 255) ? JSON.toJSONString(value).substring(0, 255) : value, e);
        }

        logger.debug("redis hset success, {}[{}]={}", key, field,
                (value != null && JSON.toJSONString(value).length() > 255) ? JSON.toJSONString(value).substring(0, 255) : value);
    }

    @Override
    public void hmset(RedisDatabaseConfig dbConfig, String key, Map<String, T> fieldValues, Integer ttl) {
        if (StringUtils.isEmpty(key)) {
            logger.warn("参数不合法：key={}", key);
        }

        try {
            RedisTemplate<String, Object> redisTemplate = getRedisTemplate(dbConfig);
            adaptSerializer(redisTemplate, null,
                    fieldValues.entrySet().stream().findFirst().map(item -> item.getValue()));

            redisTemplate.opsForHash().putAll(key, fieldValues);

            if (ttl != null && ttl > 0) {
                redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.warn("hmset redis失败,key={},fieldValues={}", key,
                    (fieldValues != null && fieldValues.size() > 0 && JSON.toJSONString(fieldValues).length() > 255)
                            ? JSON.toJSONString(fieldValues).substring(0, 255) : fieldValues);
        }

        logger.debug("redis hmset success, key={}, fieldValue={}", key,
                (fieldValues != null && fieldValues.size() > 0 && JSON.toJSONString(fieldValues).length() > 255) ?
                        JSON.toJSONString(fieldValues).substring(0, 255) : fieldValues);
    }

    @Override
    public void hdel(RedisDatabaseConfig dbConfig, String key, String field) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
            logger.warn("参数不合法：key={},field={}", key, field);
        }

        try {
            RedisTemplate<String, Object> redisTemplate = getRedisTemplate(dbConfig);

            redisTemplate.opsForHash().delete(key, field);
        } catch (Exception e) {
            logger.error("hdel redis失败，key={}, field={}", key, field);
        }

        logger.debug("redis hdel success, {}[{}]", key, field);
    }

    @Override
    public void clear(RedisDatabaseConfig dbConfig, String key) {
        if (StringUtils.isEmpty(key)) {
            logger.warn("参数不合法：db或key不能为空");
        }

        try {
            RedisTemplate<String, Object> redisTemplate = getRedisTemplate(dbConfig);

            redisTemplate.delete(key);
        } catch (Exception e) {
            logger.warn("清除缓存失败: key={}", key);
        }
    }

    private <T> void setInvoke(RedisDatabaseConfig dbConfig, String key, T value, Integer ttl) {

        if(StringUtils.isEmpty(key)) {
            logger.warn("参数不合法: key={}", key);
        }

        try {
            RedisTemplate<String, Object> redisTemplate = getRedisTemplate(dbConfig);
            adaptSerializer(redisTemplate, null, value);

            if(ttl != null && ttl > 0) {
                redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
        } catch (Exception e) {
            logger.warn("put redis failed,key={},",key, e);
        }

        logger.debug("redis put success, key={}", key);
    }

    private Optional<T> getInvoke(RedisDatabaseConfig dbConfig, String key, Class clazz, Integer ttl) {
        if(StringUtils.isEmpty(key)) {
            logger.warn("参数不合法: key={}", key);
        }

        Optional<T> result = Optional.empty();
        try {
            RedisTemplate<String, Object> redisTemplate = getRedisTemplate(dbConfig);
            adaptSerializer(redisTemplate, clazz, null);

            Object resObj = redisTemplate.opsForValue().get(key);

            if (ttl != null && ttl > 0) {
                redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
            }

            result = Optional.ofNullable((T)clazz.cast(resObj));
        } catch (Exception e) {
            logger.warn("get redis failed,key={}", key, e);
        }

        return result;
    }

    public abstract RedisTemplate<String, Object> getRedisTemplate(RedisDatabaseConfig dbConfig);

    private void adaptSerializer(RedisTemplate<String, Object> redisTemplate, Class clazz, Object value) {
        if(redisTemplate == null)
            return;

        if(clazz != null && String.class.equals(clazz)) {
            redisTemplate.setValueSerializer(stringRedisSerializer);
            redisTemplate.setHashValueSerializer(stringRedisSerializer);
        } else if (value instanceof String) {
            redisTemplate.setValueSerializer(stringRedisSerializer);
            redisTemplate.setHashValueSerializer(stringRedisSerializer);
        } else {
            redisTemplate.setValueSerializer(fastJsonRedisSerializer);
            redisTemplate.setHashValueSerializer(fastJsonRedisSerializer);
        }
    }
}
