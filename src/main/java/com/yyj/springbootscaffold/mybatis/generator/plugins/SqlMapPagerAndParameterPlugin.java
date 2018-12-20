package com.yyj.springbootscaffold.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

/**
 * Created by yyj on 2018/12/20.
 * xml 中取消 Blob 逻辑,故只处理selectByExampleWithoutBLOBs即可
 */
public class SqlMapPagerAndParameterPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * 添加分页
     * 修改参数类型 BaseExample
     */
    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element,
                                                                     IntrospectedTable introspectedTable) {
        XmlElement ifLimitNotNullElement = getXmlElement("if", "test", "limit != null", null);
        XmlElement ifOffsetNotNullElement = getXmlElement("if", "test", "offset != null", "limit ${offset}, ${limit}");
        ifLimitNotNullElement.addElement(ifOffsetNotNullElement);

        XmlElement ifOffsetNullElement = getXmlElement("if", "test", "offset == null", "limit ${limit}");
        ifLimitNotNullElement.addElement(ifOffsetNullElement);

        element.addElement(ifLimitNotNullElement);

        if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3) {
            changeParameterType(element);
        }

        return true;
    }

    private XmlElement getXmlElement(String name, String attributeName, String attributeValue, String textElement) {
        XmlElement xmlElement = new XmlElement(name);
        xmlElement.addAttribute(new Attribute(attributeName, attributeValue));

        if(textElement != null)
            xmlElement.addElement(new TextElement(textElement));

        return xmlElement;
    }

    //修改参数类型BaseExample
    @Override
    public boolean sqlMapDeleteByExampleElementGenerated(XmlElement element,
                                                         IntrospectedTable introspectedTable) {
        if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3) {
            changeParameterType(element);
        }

        return true;
    }

    //修改参数类型BaseExample
    @Override
    public boolean sqlMapCountByExampleElementGenerated(XmlElement element,
                                                        IntrospectedTable introspectedTable) {
        if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3) {
            changeParameterType(element);
        }

        return true;
    }

    private void changeParameterType(XmlElement element) {
        boolean removeSign = element.getAttributes().removeIf(item -> "parameterType".equals(item.getName()));

        //确定路径部分,需修改
        if(removeSign)
            element.addAttribute(new Attribute("parameterType", "com.yyj.springbootscaffold.mybatis.query.BaseExample"));
    }
}
