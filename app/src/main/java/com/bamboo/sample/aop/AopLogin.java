package com.bamboo.sample.aop;

import com.bamboo.aspectj.FastAop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 项目名称：aop-sample
 *
 * @Author bamboolife
 * 邮箱：core_it@163.com
 * 创建时间：2020-01-28 21:06
 * 描述：
 */
@FastAop
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AopLogin {
}
