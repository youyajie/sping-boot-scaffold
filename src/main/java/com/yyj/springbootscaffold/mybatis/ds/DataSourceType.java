package com.yyj.springbootscaffold.mybatis.ds;

/**
 * Created by yyj on 2018/12/27.
 * 项目数据源基础配置项:主库和从库
 * 需要在数据库配置处设置配置项的值
 */
public enum DataSourceType {
    MASTER("master"), SLAVE("slave");

    private String value;
    DataSourceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
