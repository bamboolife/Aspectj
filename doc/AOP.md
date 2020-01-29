## 一、简述

### 1、AOP的概念
> AOP为Aspect Oriented Programming的缩写，意为：面向切面编程，通过预编译方式和运行期动态代理实现程序功能的统一维护的一种技术。AOP是OOP的延续，是软件开发中的一个热点，也是Spring框架中的一个重要内容，是函数式编程的一种衍生范型。利用AOP可以对业务逻辑的各个部分进行隔离，从而使得业务逻辑各部分之间的耦合度降低，提高程序的可重用性，同时提高了开发的效率。

### 2、项目场景

项目开发过程中，可能会有这样的需求，需要我们在方法执行完成后，记录日志（后台开发中比较常见~），或是计算这个方法的执行时间，在不使用AOP的情况下，我们可以在方法最后调用另一个专门记录日志的方法，或是在方法体的首尾分别获取时间，然后通过计算时间差来计算整个方法执行所消耗的时间，这样也可以完成需求。那如果不只一个方法要这么玩怎么办？每个方法都写上一段相同的代码吗？后期处理逻辑变了要怎么办？最后老板说这功能不要了我们还得一个个删除？

很明显，这是不可能的，我们不仅仅是代码的搬运工，我们还是有思考能力的软件开发工程师。这么low的做法绝对不干，这种问题我们完全可以用AOP来解决，不就是在方法前和方法后插入一段代码吗？AOP分分钟搞定。

### 3、AOP的实现方式
AOP仅仅只是个概念，实现它的方式（工具和库）有以下几种：

- AspectJ: 一个 JavaTM 语言的面向切面编程的无缝扩展（适用Android）。
- avassist for Android: 用于字节码操作的知名 java 类库 Javassist 的 Android 平台移植版。
- DexMaker: Dalvik 虚拟机上，在编译期或者运行时生成代码的 Java API。
- ASMDEX: 一个类似 ASM 的字节码操作库，运行在Android平台，操作Dex字节码。

此工程主要使用AspectJ的方式在Android开发中实现

## 二、AspectJ的引入

Android Studio需要在app模块的build.gradle文件中引入，总共分为3个步骤：

#### 1）添加核心依赖
```gradle
dependencies {
    ...
    compileOnly 'org.aspectj:aspectjrt:1.8.9'
}
```
#### 2）编写gradle编译脚本
```gradle
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'org.aspectj:aspectjtools:1.8.9'
        classpath 'org.aspectj:aspectjweaver:1.8.9'
    }
}
```
> AspectJ需要依赖maven仓库。

#### 3）添加gradle任务
```gradle
dependencies {
    ...
}
// 贴上面那段没用的代码是为了说明：下面的任务代码与dependencies同级

import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
final def log = project.logger
final def variants = project.android.applicationVariants

variants.all { variant ->
    if (!variant.buildType.isDebuggable()) {
        log.debug("Skipping non-debuggable build type '${variant.buildType.name}'.")
        return;
    }

    JavaCompile javaCompile = variant.javaCompile
    javaCompile.doLast {
        String[] args = ["-showWeaveInfo",
                         "-1.8",
                         "-inpath", javaCompile.destinationDir.toString(),
                         "-aspectpath", javaCompile.classpath.asPath,
                         "-d", javaCompile.destinationDir.toString(),
                         "-classpath", javaCompile.classpath.asPath,
                         "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)]
        log.debug "ajc args: " + Arrays.toString(args)

        MessageHandler handler = new MessageHandler(true);
        new Main().run(args, handler);
        for (IMessage message : handler.getMessages(null, true)) {
            switch (message.getKind()) {
                case IMessage.ABORT:
                case IMessage.ERROR:
                case IMessage.FAIL:
                    log.error message.message, message.thrown
                    break;
                case IMessage.WARNING:
                    log.warn message.message, message.thrown
                    break;
                case IMessage.INFO:
                    log.info message.message, message.thrown
                    break;
                case IMessage.DEBUG:
                    log.debug message.message, message.thrown
                    break;
            }
        }
    }
}

```
> 直接粘贴到build.gradle文件的末尾即可，不要嵌套在别的指令中。

## 三、AOP的基本知识

### 1、AOP术语
1. 通知、增强处理（Advice）：就是你想要的功能，也就是上面说的日志、耗时计算等。
2. 连接点（JoinPoint）：允许你通知（Advice）的地方，那可就真多了，基本每个方法的前、后（两者都有也行），或抛出异常是时都可以是连接点（spring只支持方法连接点）。AspectJ还可以让你在构造器或属性注入时都行，不过一般情况下不会这么做，只要记住，和方法有关的前前后后都是连接点。
3. 切入点（Pointcut）：上面说的连接点的基础上，来定义切入点，你的一个类里，有15个方法，那就有十几个连接点了对吧，但是你并不想在所有方法附件都使用通知（使用叫织入，下面再说），你只是想让其中几个，在调用这几个方法之前、之后或者抛出异常时干点什么，那么就用切入点来定义这几个方法，让切点来筛选连接点，选中那几个你想要的方法。
4. 切面（Aspect）：切面是通知和切入点的结合。现在发现了吧，没连接点什么事，连接点就是为了让你好理解切点搞出来的，明白这个概念就行了。通知说明了干什么和什么时候干（什么时候通过before，after，around等AOP注解就能知道），而切入点说明了在哪干（指定到底是哪个方法），这就是一个完整的切面定义。
5. 织入（weaving） 把切面应用到目标对象来创建新的代理对象的过程。
### 2、AOP注解与使用
- @Aspect：声明切面，标记类
- @Pointcut(切点表达式)：定义切点，标记方法
- @Before(切点表达式)：前置通知，切点之前执行
- @Around(切点表达式)：环绕通知，切点前后执行
- @After(切点表达式)：后置通知，切点之后执行
- @AfterReturning(切点表达式)：返回通知，切点方法返回结果之后执行
- @AfterThrowing(切点表达式)：异常通知，切点抛出异常时执行


