package com.bamboo.sample;

import android.app.Application;

import com.bamboo.fastaop.FastAop;
import com.bamboo.sample.aop.AopHelper;

/**
 * 项目名称：aop-sample
 *
 * @Author bamboolife
 * 邮箱：core_it@163.com
 * 创建时间：2020-01-28 20:58
 * 描述：
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initFastAop();
    }

    private void initFastAop() {
        FastAop.init(new AopHelper(this));
    }

}
