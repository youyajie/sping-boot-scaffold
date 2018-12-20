package com.yyj.springbootscaffold.mybatis.query;

import com.yyj.springbootscaffold.mybatis.generator.Column;
import com.yyj.springbootscaffold.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * 将 HttpServletRequest 中的查询参数转成指定类属性查询的 BaseExample
 * 查询参数形式url?name_eq=aaa -> name字段对应的数据库字段=aaa
 * createdTime_t_eq=2018-12-20 20:00:00 -> createdTime 字段对应的数据库字段=UNIX_TIMESTAMP(2018-12-20 20:00:00)
 * Created by yyj on 2018/12/20.
 */
public class QueryBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryBuilder.class);
    private static final String page = "page";
    private static final String perPage = "per_page";
    private static final String order = "order";
    private static final String direction = "direction";

    public static BaseExample buildExample(HttpServletRequest request, Class clazz) {
        BaseExample example = new BaseExample();

        //默认分页
        if(request == null || request.getParameterMap() == null) {
            example.setOffset(0);
            example.setLimit(20);
        }

        buildExampleCriteria(request, clazz, example);

        return example;
    }

    private static void buildExampleCriteria(HttpServletRequest request, Class clazz, BaseExample example) {
        Map<String, String[]> parameterMap = request.getParameterMap();

        String queryKey, queryValue = "";
        Integer currentPage = 1, currentPerPage = 20;
        String orderByField = "", orderDirection = "";
        for(Map.Entry<String, String[]> parameter : parameterMap.entrySet()) {
            queryKey = parameter.getKey();
            if(parameter.getValue() instanceof String[]) {
                String[] parameterValues = parameter.getValue();
                queryValue = parameterValues == null || parameterValues.length == 0 ? null :
                        parameterValues[parameterValues.length - 1];
            }

            if(StringUtils.isEmpty(queryValue) || StringUtils.isEmpty(queryKey))
                continue;

            if(page.equals(queryKey)) {
                currentPage = Integer.valueOf(queryValue);
            } else if(perPage.equals(queryKey)) {
                currentPerPage = Integer.valueOf(queryValue);
            } else if(order.equals(queryKey)) {
                orderByField = queryValue;
            } else if(direction.equals(queryKey)) {
                orderDirection = queryValue;
            } else {
                buildFieldCriteria(queryKey, queryValue, example, clazz);
            }
        }

        Integer offset = (currentPage - 1) * currentPerPage;
        example.setOffset(offset);
        example.setLimit(currentPerPage);

        if(!StringUtils.isEmpty(orderByField) && !StringUtils.isEmpty(orderDirection)) {
            example.setOrderByClause(String.format("%s %s", orderByField, orderDirection));
        }
    }

    private static void buildFieldCriteria(String queryKey, String queryValue, BaseExample example, Class clazz) {
        String fieldName, op, type = "";

        Matcher matcher = QueryOperatorEnum.getAllOperationPattern().matcher(queryKey);
        if (matcher.find()) {
            fieldName = queryKey.substring(0, matcher.start());
            if(fieldName.contains("_")) {
                String[] fieldWithType = fieldName.split("_");
                if(fieldWithType.length == 2) {
                    fieldName = fieldWithType[0];
                    type = fieldWithType[1];
                }
            }
            op = matcher.group().replaceAll("_", "");
        } else {
            fieldName = queryKey;
            op = QueryOperatorEnum.eq.name();
        }

        if(!StringUtils.isEmpty(type))
            queryValue = convertFieldType(queryValue, type);

        String column = getDBFieldName(fieldName, clazz);
        String dbOp = "";
        try {
            dbOp = QueryOperatorEnum.valueOf(op).getDbOp();
        } catch (Exception e) {
            LOGGER.error("查询参数[" + fieldName + "]算子[" + op + "]错误!");
            e.printStackTrace();
        }

        example.addCriterion(column, dbOp, queryValue, fieldName);
    }

    private static String convertFieldType(String queryValue, String type) {
        if(StringUtils.isEmpty(type))
            return queryValue;

        switch (type) {
            case "t":
                Long timeStamp = TimeUtils.getTimeStamp(queryValue);
                queryValue = timeStamp != null ? timeStamp.toString() : queryValue;
                break;
            default:
                break;
        }

        return queryValue;
    }

    private static String getDBFieldName(String fieldName, Class clazz) {
        String dbFieldName = "";

        Field field = getFieldByName(fieldName, clazz);
        if(field == null)
            return dbFieldName;

        Column column = field.getAnnotation(Column.class);
        dbFieldName = column.value();

        return dbFieldName;
    }

    private static Field getFieldByName(String fieldName, Class clazz) {
        Field field = null;

        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            LOGGER.warn("查询转换当前类中不存在该字段:{}, class:{}", fieldName, clazz.getName() );
        }

        if(field != null)
            return field;

        Class currentClazz = clazz;
        int n = 0;
        while (currentClazz.getSuperclass() != null && n < 50) {
            currentClazz = clazz.getSuperclass();

            try {
                field = currentClazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                LOGGER.warn("查询转换当前类中不存在该字段:{}, class:{}", fieldName, clazz.getName() );
            }

            if(field != null)
                return field;

            n++;
        }

        return field;
    }
}
