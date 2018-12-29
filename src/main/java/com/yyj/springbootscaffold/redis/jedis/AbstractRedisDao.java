package com.yyj.springbootscaffold.redis.jedis;

import com.yyj.springbootscaffold.redis.RedisDatabaseEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by yyj on 2018/12/28.
 */
public abstract class AbstractRedisDao implements IRedisDao {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void put(RedisDatabaseEnum db, String key, Object value, Integer ttl) {
        putBuilder(db, key, value, ttl);
    }

    @Override
    public void put(RedisDatabaseEnum db, String key, Object value) {
        putBuilder(db, key, value, null);
    }

    private void putBuilder(RedisDatabaseEnum db, String key, Object value, Integer ttl) {
        if(StringUtils.isEmpty(key) || value == null) {
            logger.warn("参数不合法: db={},key={},", db == null ? "0" : db.getDatabaseId(), key);
        }

        try {
            RedisTemplate<String, Object> redisTemplate = getRedisTemplate();

            if(db != null && !RedisDatabaseEnum.ZERO.equals(db)) {
                redisTemplate.execute((RedisCallback)(connection) -> {
                    //redis事务,目前需要手动close
                    try {
                        connection.select(db.getDatabaseId());
                        if(ttl != null && ttl > 0) {
                            redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
                        } else {
                            redisTemplate.opsForValue().set(key, value);
                        }
                    } catch (Exception e) {
                        logger.warn("put redis failed,key={}",key);
                    } finally {
                        connection.close();
                    }
                    return null;
                });
            } else {
                if(ttl != null && ttl > 0) {
                    redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
                } else {
                    redisTemplate.opsForValue().set(key, value);
                }
            }
        } catch (Exception e) {
            logger.warn("put redis failed,key={}",key);
        }

        logger.debug("redis put success, db={},key={}", db == null ? "0" : db.getDatabaseId(), key);
    }

    @Override
    public <T> T get(RedisDatabaseEnum db, String key, Class clazz, Integer ttl) {
        return invokeGet(db, key, clazz, ttl);
    }

    @Override
    public <T> T get(RedisDatabaseEnum db, String key, Class clazz) {
        return invokeGet(db, key, clazz, null);
    }

    private <T> T invokeGet(RedisDatabaseEnum db, String key, Class clazz, Integer ttl) {
        Object result = null;

        try {
            RedisTemplate<String, Object> redisTemplate = getRedisTemplate();

            if(db != null && !RedisDatabaseEnum.ZERO.equals(db)) {
                result = redisTemplate.execute((RedisCallback) (connection) -> {
                    Object obj = null;

                    //redis事务,目前需要手动close
                    try {
                        connection.select(db.getDatabaseId());
                        obj = redisTemplate.opsForValue().get(key);

                        if (ttl != null && ttl > 0) {
                            redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
                        }
                    } catch (Exception e) {
                        logger.warn("get redis failed,key={}", key);
                    } finally {
                        connection.close();
                    }
                    return obj;
                });
            } else {
                result = redisTemplate.opsForValue().get(key);
                if (ttl != null && ttl > 0) {
                    redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
                }
            }
        } catch (Exception e) {
            logger.warn("get redis failed,key={}", key);
        }

        return (T)clazz.cast(result);
    }

    @Override
    public String hget(RedisDatabaseEnum db, String key, String field) {
        if (db == null || StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
            logger.warn("参数不合法：db={},key={},field={}", db == null ? "null" : db.getDatabaseId(), key, field);
        }

        String result = "";
        try {
            RedisTemplate<String, Object> redisTemplate = getRedisTemplate();

            if(db != null && !RedisDatabaseEnum.ZERO.equals(db)) {
                result = redisTemplate.execute((RedisCallback<String>) (connection) -> {
                    Object obj = null;

                    //redis事务,目前需要手动close
                    try {
                        connection.select(db.getDatabaseId());
                        obj = redisTemplate.opsForHash().get(key, field);
                    } catch (Exception e) {
                        logger.warn("hget redis failed,key={}", key);
                    } finally {
                        connection.close();
                    }
                    return obj != null ? obj.toString() : "";
                });
            } else {
                Object obj = redisTemplate.opsForHash().get(key, field);
                result = obj != null ? obj.toString() : "";
            }
        } catch (Exception e) {
            logger.warn("hget redis failed,key={}", key);
        }

        return result;
    }

    @Override
    public void hmset(RedisDatabaseEnum db, String key, Map<String, String> fieldValues, Integer ttl) {
        if (db == null || key == null || key.trim().equals("")) {
            logger.warn("参数不合法：db={},key={}", db == null ? "null" : db.getDatabaseId(), key);
        }

        try {
            RedisTemplate<String, Object> redisTemplate = getRedisTemplate();

            if(db != null && !RedisDatabaseEnum.ZERO.equals(db)) {
                redisTemplate.execute((RedisCallback) (connection) -> {
                    //redis事务,目前需要手动close
                    try {
                        connection.select(db.getDatabaseId());
                        redisTemplate.opsForHash().putAll(key, fieldValues);
                        if (ttl != null && ttl > 0) {
                            redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
                        }
                    } catch (Exception e) {
                        logger.warn("hmset redis失败,key={},fieldValues={}", key,
                                (fieldValues != null && fieldValues.size() > 0 && fieldValues.toString().length() > 255)
                                        ? fieldValues.toString().substring(0, 255) : fieldValues);
                    } finally {
                        connection.close();
                    }
                    return null;
                });
            } else {
                redisTemplate.opsForHash().putAll(key, fieldValues);
                if (ttl != null && ttl > 0) {
                    redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
                }
            }
        } catch (Exception e) {
            logger.warn("hmset redis失败,key={},fieldValues={}", key,
                    (fieldValues != null && fieldValues.size() > 0 && fieldValues.toString().length() > 255)
                            ? fieldValues.toString().substring(0, 255) : fieldValues);
        }

        logger.debug("redis hmset success, db={}, key={}, fieldValue={}", db.getDatabaseId(), key,
                (fieldValues != null && fieldValues.size() > 0 && fieldValues.toString().length() > 255) ?
                        fieldValues.toString().substring(0, 255) : fieldValues);
    }

    @Override
    public List<String> mget(RedisDatabaseEnum db, List<String> keys) {
        if (db == null || CollectionUtils.isEmpty(keys)) {
            logger.warn("参数不合法：db={},key={}", db == null ? "null" : db.getDatabaseId(), keys);
        }

        List<String> re = null;
        try {
            RedisTemplate<String, Object> redisTemplate = getRedisTemplate();

            List<Object> reObj = null;
            if(db != null && !RedisDatabaseEnum.ZERO.equals(db)) {
                reObj = redisTemplate.execute((RedisCallback<List<Object>>) (connection) -> {
                    List<Object> results = null;

                    //redis事务,目前需要手动close
                    try {
                        connection.select(db.getDatabaseId());
                        results = redisTemplate.opsForValue().multiGet(keys);
                    } catch (Exception e) {
                        logger.warn("mget redis失败,key={}", keys);
                    } finally {
                        connection.close();
                    }
                    return results;
                });
            } else {
                reObj = redisTemplate.opsForValue().multiGet(keys);
            }

            if(!CollectionUtils.isEmpty(reObj)) {
                re = reObj.stream().map(e -> e==null?"":e.toString()).collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.warn("mget redis失败,key={}", keys);
        }

        return re;
    }

    @Override
    public Boolean sismember(RedisDatabaseEnum db, String key, Object value) {
        if (db == null || StringUtils.isEmpty(key) || value == null) {
            logger.warn("参数不合法：db={},key={},value={}", db == null ? "null" : db.getDatabaseId(), key,value);
        }

        Object re = false;
        try {
            RedisTemplate<String, Object> redisTemplate = getRedisTemplate();

            if(db != null && !RedisDatabaseEnum.ZERO.equals(db)) {
                re = redisTemplate.execute((RedisCallback) (connection) -> {
                    //redis事务,目前需要手动close
                    Object isMember = "";
                    try {
                        connection.select(db.getDatabaseId());
                        isMember = redisTemplate.opsForSet().isMember(key, value);
                    } catch (Exception e) {
                        logger.warn("sismember redis失败,key={},value={}", key,value);
                    } finally {
                        connection.close();
                    }
                    return isMember;
                });
            } else {
                re = redisTemplate.opsForSet().isMember(key, value);
            }
        } catch (Exception e) {
            logger.warn("sismember redis失败,key={},value={}", key,value);
        }

        return (Boolean)re;
    }

    @Override
    public void srem(RedisDatabaseEnum db, String key, Object[] values) {
        if(db == null || StringUtils.isEmpty(key) || values == null || values.length == 0) {
            logger.warn("参数不合法：db={},key={},values={}", db == null ? "null" : db.getDatabaseId(), key,values);
        }
        try {
            RedisTemplate<String, Object> redisTemplate = getRedisTemplate();
            if(db != null && !RedisDatabaseEnum.ZERO.equals(db)) {
                redisTemplate.execute((RedisCallback) (connection) -> {
                    //redis事务,目前需要手动close
                    Object success = null;
                    try {
                        connection.select(db.getDatabaseId());
                        success = redisTemplate.opsForSet().remove(key, values);
                    } catch (Exception e) {
                        logger.warn("remove redis失败,key={},values={}", key,values);
                    } finally {
                        connection.close();
                    }
                    return success;
                });
            } else {
                redisTemplate.opsForSet().remove(key, values);
            }
        } catch (Exception e) {
            logger.warn("remove redis失败,key={},value={}", key,values);
        }

        logger.debug("redis srem success, db={},key={}", db == null ? "0" : db.getDatabaseId(), key);
    }

    @Override
    public void sadd(RedisDatabaseEnum db, String key, Object[] values) {
        if(db == null || StringUtils.isEmpty(key) || values == null || values.length == 0) {
            logger.warn("参数不合法：db={},key={},values={}", db == null ? "null" : db.getDatabaseId(), key,values);
        }
        try {
            RedisTemplate<String, Object> redisTemplate = getRedisTemplate();

            if(db != null && !RedisDatabaseEnum.ZERO.equals(db)) {
                redisTemplate.execute((RedisCallback) (connection) -> {
                    //redis事务,目前需要手动close
                    Object success = null;
                    try {
                        connection.select(db.getDatabaseId());
                        success = redisTemplate.opsForSet().add(key, values);
                    } catch (Exception e) {
                        logger.warn("sadd redis失败,key={},values={}", key,values);
                    } finally {
                        connection.close();
                    }
                    return success;
                });
            } else {
                redisTemplate.opsForSet().add(key, values);
            }
        } catch (Exception e) {
            logger.warn("sadd redis失败,key={},value={}", key,values);
        }

        logger.debug("redis add success, db={},key={}", db == null ? "0" : db.getDatabaseId(), key);
    }

    @Override
    public Integer clear(RedisDatabaseEnum db, String key) {
        logger.debug("删除键，db={},key={}",db!=null?db.getDatabaseId():0,key);
        if(db == null || StringUtils.isEmpty(key)) {
            logger.warn("参数不合法：db={},key={}", db == null ? "null" : db.getDatabaseId(), key);
        }
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();
        Set<Object> execute = redisTemplate.execute(new RedisCallback<Set<Object>>() {

            @Override
            public Set<Object> doInRedis(RedisConnection connection) throws DataAccessException {
                connection.select(db.getDatabaseId());
                Cursor<byte[]> cursor = connection.scan( new ScanOptions.ScanOptionsBuilder().match(key).count(1000).build());
                Set<Object> binaryKeys = new HashSet<>();
                while (cursor.hasNext()) {
                    byte[] next = cursor.next();
                    binaryKeys.add(next);
                }
                return binaryKeys;
            }
        });
        logger.debug("匹配到的key数目={}",execute.size());

        List<String> keys = new ArrayList<>();
        for (Object object : execute) {
            byte[] arr = (byte[])object;
            String key1 = new String(arr);
            keys.add(key1);
        }
        for (String string : keys) {
            logger.debug(string);
        }
        if(db != null && !RedisDatabaseEnum.ZERO.equals(db)) {
            redisTemplate.execute((RedisCallback) (connection) -> {
                //redis事务,目前需要手动close
                Object success = null;
                try {
                    connection.select(db.getDatabaseId());
                    redisTemplate.delete(keys);
                } catch (Exception e) {
                    logger.warn("delete redis失败,key={}", key);
                } finally {
                    connection.close();
                }
                return success;
            });
        } else {
            redisTemplate.delete(keys);
        }
        logger.debug("redis delete success, db={},key={}", db == null ? "0" : db.getDatabaseId(), key);
        return execute.size();
    }

    abstract RedisTemplate<String, Object> getRedisTemplate();
}
