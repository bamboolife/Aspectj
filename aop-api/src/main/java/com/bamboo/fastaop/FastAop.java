package com.bamboo.fastaop;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 项目名称：aop-sample
 *
 * @Author bamboolife
 * 邮箱：core_it@163.com
 * 创建时间：2020-01-26 10:03
 * 描述：
 */
public class FastAop {
    private static PointInterceptor mInterceptor;

    public static void init(PointInterceptor mInterceptor) {
        if (null == mInterceptor) {
            throw new IllegalArgumentException("mInterceptor can not be null");
        }
        FastAop.mInterceptor = mInterceptor;
    }

    public static void interceptor(Class clazz, ProceedingJoinPoint joinPoint){
        mInterceptor.intercept(clazz,joinPoint);
    }
}
