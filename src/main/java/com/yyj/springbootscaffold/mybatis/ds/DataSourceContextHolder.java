package com.yyj.springbootscaffold.mybatis.ds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yyj on 2018/12/27.
 */
public class DataSourceContextHolder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceContextHolder.class);
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    public static void setDataSourceType(String dataSourceType) {
        if(dataSourceType == null) {
            LOGGER.error("设置数据源类型失败,设置类型参数为空");
        }

        contextHolder.set(dataSourceType);
    }

    public static String getDataSourceType() {
        return contextHolder.get();
    }

    public static void clearDataSourceType() {
        contextHolder.remove();
    }

}
