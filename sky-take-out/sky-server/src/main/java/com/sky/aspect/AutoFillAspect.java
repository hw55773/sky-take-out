package com.sky.aspect;

import com.sky.anotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * className:       AutoFillAspect
 * author:          wenhao2002
 * date:            2024/5/20 19:30
 */
@Slf4j
@Component
@Aspect
public class AutoFillAspect {

    /**
     * 切点表达式
     */
    @Pointcut("@annotation(com.sky.anotation.AutoFill)")
    public void autoFillPointCut(){}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始公共字段的填充");
        //获取被拦截的方法上面的操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill=signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType= autoFill.value();
        //获取拦截方法上的实体对象
        Object[] args = joinPoint.getArgs();
        Object entity=args[0];
        //准备对应的赋值数据
        LocalDateTime localDateTime=LocalDateTime.now();
        Long id= BaseContext.getCurrentId();
        //根据不同类型，通过反射为其赋值
        if (operationType==OperationType.INSERT){
            try {
                //通过反射获取对应方法
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //为其实体对象赋值
                setCreateTime.invoke(entity,localDateTime);
                setCreateUser.invoke(entity,id);
                setUpdateTime.invoke(entity,localDateTime);
                setUpdateUser.invoke(entity,id);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        if (operationType==OperationType.UPDATE){
            try {
                //通过反射获取对应方法
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //为其实体对象赋值
                setUpdateTime.invoke(entity,localDateTime);
                setUpdateUser.invoke(entity,id);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }


}
