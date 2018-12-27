package com.yyj.springbootscaffold.mybatis.ds;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Created by yyj on 2018/12/27.
 * 动态设置数据源,未设置时选择配置的从库
 */
public class DataSourceRouter extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDataSourceType() != null ? DataSourceContextHolder.getDataSourceType()
                : DataSourceType.SLAVE.getValue();
    }
}
