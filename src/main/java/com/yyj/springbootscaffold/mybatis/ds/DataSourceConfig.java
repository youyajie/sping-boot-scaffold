package com.yyj.springbootscaffold.mybatis.ds;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;

/**
 * Created by yyj on 2018/12/27.
 */
@Configuration
public class DataSourceConfig {
    @Bean
    @Primary
    @ConfigurationProperties("datasource.yyj.master")
    public DataSourceProperties masterDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("datasource.yyj.master")
    public DataSource masterDataSource() {
        return masterDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    @ConfigurationProperties("datasource.yyj.slave")
    public DataSourceProperties slaveDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("datasource.yyj.slave")
    public DataSource slaveDataSource() {
        return slaveDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    @ConfigurationProperties("datasource.second.slave")
    public DataSourceProperties secondSlaveDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("datasource.second.slave")
    public DataSource secondSlaveDataSource() {
        return secondSlaveDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public DataSource routeDataSource() {
        return new DataSourceRouter() {{
            setDefaultTargetDataSource(slaveDataSource());
            setTargetDataSources(new HashMap<Object, Object>() {{
                put(DataSourceType.MASTER.getValue(), masterDataSource());
                put(DataSourceType.SLAVE.getValue(), slaveDataSource());
                put("SecondDataSource", secondSlaveDataSource());
            }});
        }};
    }

    @Bean
    @Primary
    public LazyConnectionDataSourceProxy lazyConnectionDataSourceProxy() {
        return new LazyConnectionDataSourceProxy(routeDataSource());
    }
}
