package com.yyj.springbootscaffold.redis.jedis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Created by yyj on 2018/12/28.
 */
@Service
@Qualifier("crmRedisDao")
public class CrmRedisDao extends AbstractRedisDao {

    @Autowired
    private RedisTemplate<String, Object> crmRedisTemplate;

    RedisTemplate<String, Object> getRedisTemplate() {
        return crmRedisTemplate;
    }
}
