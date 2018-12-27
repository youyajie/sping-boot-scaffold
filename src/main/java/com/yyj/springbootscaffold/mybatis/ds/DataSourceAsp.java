package com.yyj.springbootscaffold.mybatis.ds;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Created by yyj on 2018/12/27.
 */
@Aspect
@Component
public class DataSourceAsp {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Around("execution(* com.*..dao..*.*(..))")
    public Object switchDataSource(ProceedingJoinPoint joinPoint) {
        //参数不为空校验
        paramValidate(joinPoint);

        Class mapperClass = joinPoint.getSignature().getDeclaringType();
        Annotation[] annotations =  mapperClass.getAnnotations();

        //读取Mapper类注解标注的数据源(项目对应数据库的主从不设置注解)
        String dsName = "";
        if(annotations != null && annotations.length > 0) {
            for(Annotation annotation : annotations) {
                String annotationName = annotation.annotationType().getSimpleName();
                if(annotation == null || StringUtils.isEmpty(annotationName))
                    continue;

                if(annotationName.toLowerCase().contains("datasource")) {
                    dsName = annotation.annotationType().getSimpleName();
                    break;
                }
            }
        }

        //是否在事务中
        boolean transactionSign = TransactionSynchronizationManager.isActualTransactionActive();

        String methodName = joinPoint.getSignature().getName();

        if(!StringUtils.isEmpty(dsName)) {
            DataSourceContextHolder.setDataSourceType(dsName);
            logger.info("dataSource asp: switch dataSource to {} in method {}", dsName, joinPoint.getSignature());
        } else if (transactionSign || checkMasterMethod(methodName)) {
            DataSourceContextHolder.setDataSourceType(DataSourceType.MASTER.getValue());
            logger.info("dataSource asp: switch dataSource to {} in method {}", DataSourceContextHolder.getDataSourceType(),
                    joinPoint.getSignature());
        } else {
            DataSourceContextHolder.setDataSourceType(DataSourceType.SLAVE.getValue());
            logger.info("dataSource asp: switch dataSource to {} in method {}", DataSourceContextHolder.getDataSourceType(),
                    joinPoint.getSignature());
        }

        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            logger.error("dataSource asp: 执行Mapper层方法失败", throwable.toString());
            throwable.printStackTrace();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }

        return result;
    }

    private void paramValidate(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Object[] parameters = joinPoint.getArgs();
        if(parameters == null || parameters.length <= 0)
            return;

        Annotation[][] paramAnnotations = signature.getMethod().getParameterAnnotations();

        List<Integer> notNullIndex = new ArrayList<>();
        for(int i = 0; i < paramAnnotations.length; i++) {
            for(Annotation paramAnnotation : paramAnnotations[i]) {
                if(paramAnnotation instanceof NotNull) {
                    notNullIndex.add(i);
                    break;
                }
            }
        }

        if(CollectionUtils.isEmpty(notNullIndex))
            return;

        for(Integer index : notNullIndex) {
            if(parameters.length <= index ) {
                logger.error("dataSource asp: mybatis参数判断失败,notNull标识位置超过参数个数");
            }

            Object parameter = parameters[index];
            if(parameter instanceof String) {
                if(StringUtils.isEmpty(parameter.toString())) {
                    logger.error("dataSource asp: {}类中第{}个参数为空", joinPoint.getSignature(), index);
                }
            } else if(parameter instanceof Collection) {
                if(CollectionUtils.isEmpty((Collection) parameter)) {
                    logger.error("dataSource asp: {}类中第{}个参数为空", joinPoint.getSignature(), index);
                }
            } else {
                if(parameter == null) {
                    logger.error("dataSource asp: {}类中第{}个参数为空", joinPoint.getSignature(), index);
                }
            }
        }
    }

    public static Boolean checkMasterMethod(String methodName) {
        if(StringUtils.isEmpty(methodName))
            return false;

        List<String> masterMethod = Arrays.asList("insert", "update", "delete", "create", "remove");

        Optional<String> masterSign = masterMethod.stream()
                .filter(item -> methodName.startsWith(item))
                .findAny();

        return masterSign.isPresent() ? true : false;
    }
}
