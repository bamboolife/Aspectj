## 为什么使用AOP编程

项目开发过程中，可能会有这样的需求，需要我们在方法执行完成后，记录日志（后台开发中比较常见~），或是计算这个方法的执行时间，在不使用AOP的情况下，我们可以在方法最后调用另一个专门记录日志的方法，或是在方法体的首尾分别获取时间，然后通过计算时间差来计算整个方法执行所消耗的时间，这样也可以完成需求。那如果不只一个方法要这么玩怎么办？每个方法都写上一段相同的代码吗？后期处理逻辑变了要怎么办？最后老板说这功能不要了我们还得一个个删除？

很明显，这是不可能的，我们不仅仅是代码的搬运工，我们还是有思考能力的软件开发工程师。这么low的做法绝对不干，这种问题我们完全可以用AOP来解决，不就是在方法前和方法后插入一段代码吗？AOP分分钟搞定。

[![](https://jitpack.io/v/bamboolife/Aspectj.svg)](https://jitpack.io/#bamboolife/Aspectj)

## 首先添加依赖
1. 在根build.gradle中添加
```gradle
buildscript {
    repositories {
        maven {
<<<<<<< HEAD
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath 'gradle.plugin.com.bamboo.plugin:aop-gradle-plugin:1.0.6'
=======
            url "https://plugins.gradle.org/m2/" 
        }
    }
    dependencies {
        classpath 'gradle.plugin.com.bamboo.plugin:aop-gradle-plugin:1.0.3' 
>>>>>>> e8bcef0d1097e0f858f089475fcfa4cc5c0993ee
    }
}
allprojects {
    repositories {
<<<<<<< HEAD
        maven { url 'https://jitpack.io' }
=======
        maven { url 'https://jitpack.io' }  
>>>>>>> e8bcef0d1097e0f858f089475fcfa4cc5c0993ee
    }
}
```
2. 在module中添加
```gradle
apply plugin: 'com.bamboo.aspectjrt'


dependencies {
    implementation 'com.github.bamboolife.Aspectj:aop-api:1.0.2'
    annotationProcessor 'com.github.bamboolife.Aspectj:aop-compiler:1.0.2'
}
```
## 在项目中简单的使用
### 简单使用方式一
```java
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FastAop.init(new PointInterceptor() {
            @Override
            public void intercept(Class clazz, ProceedingJoinPoint joinPoint) {
                try {
                    if (clazz == LogUtil.class) {
                        if (false) {//log开关
                            joinPoint.proceed();
                        }else{
                            Toast.makeText(MainActivity.this,"log开关已经关闭",Toast.LENGTH_SHORT).show();
                        }
                    }else if(clazz==AopLogin.class){
                        if (false){//如果已经登录
                            joinPoint.proceed();
                        }else {
                            //提示用户
                            Toast.makeText(MainActivity.this,"登录已经失效请重新登录",Toast.LENGTH_SHORT).show();
                        }

                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
        findViewById(R.id.text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: 11111111111111111");
                logPrintln();
            }
        });
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogin();
            }
        });
    }

    @AopLogin
    private void onLogin() {
        Log.i(TAG, "onLogin: ");
    }


    @LogUtil
    public void logPrintln() {
        Log.i(TAG, "intercept: 22222222222222");
    }


}

```
### 使用方式二（推荐此方式）
在Application中初始，可以通过路由管理和eventbus来管理页面的跳转，所有需要处理的业务逻辑统一管理
```java
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
//-----------添加辅助类统一管理-------------------
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


```
