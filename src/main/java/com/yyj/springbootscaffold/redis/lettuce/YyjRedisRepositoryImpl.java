package com.yyj.springbootscaffold.redis.lettuce;

import com.yyj.springbootscaffold.redis.RedisDatabaseEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Created by yyj on 2018/12/29.
 */
@Service
@Qualifier("crmRedisRepository")
public class YyjRedisRepositoryImpl extends AbstractRedisRepository {
    @Autowired
    @Qualifier("yyjZeroRedisTemplate")
    private RedisTemplate<String, Object> yyjZeroRedisTemplate;

    @Autowired
    @Qualifier("yyjOneRedisTemplate")
    private RedisTemplate<String, Object> yyjOneRedisTemplate;

    @Override
    public RedisTemplate<String, Object> getRedisTemplate(RedisDatabaseConfig redisDatabaseConfig) {
        if(redisDatabaseConfig == null)
            return yyjZeroRedisTemplate;

        if(RedisDatabaseEnum.ZERO.getDatabaseId() == redisDatabaseConfig.getDatabaseId()) {
            return yyjZeroRedisTemplate;
        } else if (RedisDatabaseEnum.FIRST.getDatabaseId() == redisDatabaseConfig.getDatabaseId()) {
            return yyjOneRedisTemplate;
        }

        return yyjZeroRedisTemplate;
    }
}
