package com.yyj.springbootscaffold.redis;

import com.yyj.springbootscaffold.redis.lettuce.RedisDatabaseConfig;

/**
 * Created by yyj on 2018/12/29.
 */
public enum RedisDatabaseEnum implements RedisDatabaseConfig{
    ZERO(0,"0库"),
    FIRST(1,"1库");

    private final int databaseId;
    private String name;

    RedisDatabaseEnum(int databaseId, String name) {
        this.databaseId = databaseId;
        this.name=name;
    }
    public int getDatabaseId() {
        return databaseId;
    }
    public String getName() {
        return name;
    }

    public static RedisDatabaseEnum getDataBase(Integer databaseId){
        if (databaseId == null) {
            return null;
        }
        for(RedisDatabaseEnum e : RedisDatabaseEnum.values()){
            if (e.getDatabaseId() == databaseId) {
                return e;
            }
        }
        return null;
    }
}
