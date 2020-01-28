package com.bamboo.sample;

import com.bamboo.aspectj.FastAop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * 项目名称：aop-sample
 *
 * @Author bamboolife
 * 邮箱：core_it@163.com
 * 创建时间：2020-01-26 08:32
 * 描述：
 */
@FastAop
@Target(ElementType.METHOD)
public @interface LogUtil {

}
