package com.bamboo.fastaop;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 项目名称：aop-sample
 *
 * @Author bamboolife
 * 邮箱：core_it@163.com
 * 创建时间：2020-01-26 10:09
 * 描述：
 */
public interface PointInterceptor {
    /**
     *
     * @param clazz
     * @param joinPoint
     */
    void intercept(Class clazz, ProceedingJoinPoint joinPoint);
}
