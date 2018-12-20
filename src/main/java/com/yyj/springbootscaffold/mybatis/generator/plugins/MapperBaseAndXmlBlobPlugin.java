package com.yyj.springbootscaffold.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 * Created by yyj on 2018/12/20.
 */
public class MapperBaseAndXmlBlobPlugin extends PluginAdapter {
    public boolean validate(List<String> list) {
        return true;
    }

    /**
     * mapper 类继承 BaseMapper,移除基础操作方法和无关import
     * mapper 默认 Integer 主键
     * 移除 xml 中生成Blob
     * 其中生成@Mapper 注解可用 Mybatis Generator 提供的插件
     * @return
     */
    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
                                   IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType("BaseMapper<"
                + introspectedTable.getBaseRecordType() + ",java.lang.Integer" + ">");
        interfaze.addSuperInterface(fqjt);

        interfaze.addAnnotation("@Mapper");

        //移除不需要的import
        interfaze.getImportedTypes().removeIf(item -> (!item.getFullyQualifiedName().contains(".yyj.") ||
                item.getFullyQualifiedName().contains("Example")));

        //确定的路径,需修改
        interfaze.addImportedType(new FullyQualifiedJavaType("com.yyj.springbootscaffold.mybatis.query.BaseMapper"));
        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper"));

        //移除原生成的方法
        interfaze.getMethods().clear();

        if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3) {
            introspectedTable.getBaseColumns().addAll(introspectedTable.getBLOBColumns());
            introspectedTable.getBLOBColumns().clear();
        }

        return true;
    }

}
