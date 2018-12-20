package com.yyj.springbootscaffold.mybatis.generator;

import java.lang.annotation.*;

/**
 * Created by yyj on 2018/12/20.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    /**
     * 数据库字段名称（默认会使用当前field）
     * @return
     */
    public String value();
}
