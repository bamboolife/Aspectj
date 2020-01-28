package com.bamboo.sample.aop;

import android.content.Context;
import android.widget.Toast;

import com.bamboo.fastaop.PointInterceptor;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 项目名称：aop-sample
 *
 * @Author bamboolife
 * 邮箱：core_it@163.com
 * 创建时间：2020-01-28 21:02
 * 描述：
 */
public class AopHelper implements PointInterceptor {
    private Context mContext;

    public AopHelper(Context context) {
        this.mContext = context;
    }

    @Override
    public void intercept(Class clazz, ProceedingJoinPoint joinPoint) {
        try {
            if (clazz == LogUtil.class) {
                if (false) {//log开关
                    joinPoint.proceed();
                }else{
                    Toast.makeText(mContext,"log开关已经关闭",Toast.LENGTH_SHORT).show();
                }
            }else if(clazz==AopLogin.class){
                if (false){//如果已经登录
                    joinPoint.proceed();
                }else {
                    //提示用户
                    Toast.makeText(mContext,"登录已经失效请重新登录",Toast.LENGTH_SHORT).show();
                }

            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }
}
