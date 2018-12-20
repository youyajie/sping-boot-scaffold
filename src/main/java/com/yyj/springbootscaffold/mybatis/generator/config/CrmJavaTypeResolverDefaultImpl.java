package com.yyj.springbootscaffold.mybatis.generator.config;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

import java.sql.Types;

/**
 * Created by yyj on 2018/12/20.
 */
public class CrmJavaTypeResolverDefaultImpl extends JavaTypeResolverDefaultImpl {

    public CrmJavaTypeResolverDefaultImpl() {
        super();
        //数据库的 tinyint 映射成 integer
        typeMap.put(Types.BIT, new JavaTypeResolverDefaultImpl.JdbcTypeInformation("BIT",
                new FullyQualifiedJavaType(Integer.class.getName())));
    }
}
