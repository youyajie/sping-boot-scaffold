package com.yyj.springbootscaffold.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by yyj on 2018/12/20.
 */
public class ModelAnnotationPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * model 类上添加 lombok 的@Data 注解
     * 字段上添加@Column 注解,value 为数据库字段名
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable) {
        topLevelClass.addImportedType("lombok.Data");
        topLevelClass.addAnnotation("@Data");

        addAnnotationColumn(topLevelClass, introspectedTable);
        return true;
    }

    private void addAnnotationColumn(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        //添加 import,确定路径,需修改
        FullyQualifiedJavaType typeColumn = new FullyQualifiedJavaType("com.yyj.springbootscaffold.mybatis.generator.Column");
        topLevelClass.addImportedType(typeColumn);

        //model字段名和数据库字段名映射
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
        Map<String, String> nameMapping = columns.stream()
                .filter(e -> (e != null && e.getActualColumnName() != null && e.getJavaProperty() != null))
                .collect(Collectors.toMap(IntrospectedColumn::getJavaProperty, IntrospectedColumn::getActualColumnName));

        if(nameMapping == null || nameMapping.size() <= 0)
            return;

        //遍历 model field 添加注解
        List<Field> fields = topLevelClass.getFields();

        if(fields == null || fields.size() <= 0)
            return;

        Map<String, String> commentMapping = columns.stream()
                .filter(e -> (e != null && e.getJavaProperty() != null))
                .collect(Collectors.toMap(IntrospectedColumn::getJavaProperty, IntrospectedColumn::getRemarks));

        String columnAnnotation;
        for(Field field : fields) {
            if(field == null)
                continue;

            columnAnnotation = String.format("@Column(\"%s\")", nameMapping.get(field.getName()));
            field.addAnnotation(columnAnnotation);

            if(commentMapping.get(field.getName()) != null && !commentMapping.get(field.getName()).equals(""))
                field.addJavaDocLine("//" + commentMapping.get(field.getName()));
        }
    }

    //不生成 set 方法
    @Override
    public boolean modelSetterMethodGenerated(Method method,
                                              TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                              IntrospectedTable introspectedTable,
                                              Plugin.ModelClassType modelClassType) {
        return false;
    }

    //不生成 get 方法
    @Override
    public boolean modelGetterMethodGenerated(Method method,
                                              TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                              IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return false;
    }
}
