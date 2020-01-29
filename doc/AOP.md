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

> @Pointcut、@Before、@Around、@After、@AfterReturning、@AfterThrowing需要在切面类中使用，即在使用@Aspect的类中。

### 1）切点表达式是什么？

这就是切点表达式：execution (* com.lqr..*.*(..))。切点表达式的组成如下：

```java
execution(<修饰符模式>? <返回类型模式> <方法名模式>(<参数模式>) <异常模式>?)
```
>除了返回类型模式、方法名模式和参数模式外，其它项都是可选的。
修饰符模式指的是public、private、protected，异常模式指的是NullPointException等。

对于切点表达式的理解需要多多理解，下面列出几个例子说明一下就好了：
```java
@Before("execution(public * *(..))")
public void before(JoinPoint point) {
    System.out.println("CSDN_LQR");
}
```

>匹配所有public方法，在方法执行之前打印"CSDN_LQR"。

```java
@Around("execution(* *to(..))")
public void around(ProceedingJoinPoint joinPoint) {
    System.out.println("CSDN");
    joinPoint.proceed();
    System.out.println("LQR");
}
```
> 匹配所有以"to"结尾的方法，在方法执行之前打印"CSDN"，在方法执行之后打印"LQR"。

```java
@After("execution(* com.lqr..*to(..))")
public void after(JoinPoint point) {
    System.out.println("CSDN_LQR");
}
```
> 匹配com.lqr包下及其子包中以"to"结尾的方法，在方法执行之后打印"CSDN_LQR"。

```java
@AfterReturning("execution(int com.lqr.*(..))")
public void afterReturning(JoinPoint point, Object returnValue) {
    System.out.println("CSDN_LQR");
}
```
> 匹配com.lqr包下所有返回类型是int的方法，在方法返回结果之后打印"CSDN_LQR"。
```java
@AfterThrowing(value = "execution(* com.lqr..*(..))", throwing = "ex")
public void afterThrowing(Throwable ex) {
    System.out.println("ex = " + ex.getMessage());
}
```
> 匹配com.lqr包及其子包中的所有方法，当方法抛出异常时，打印"ex = 报错信息"。

### 2）@Pointcut的使用

@Pointcut是专门用来定义切点的，让切点表达式可以复用。
你可能需要在切点执行之前和切点报出异常时做些动作（如：出错时记录日志），可以这么做：
```java
@Before("execution(* com.lqr..*(..))")
public void before(JoinPoint point) {
    System.out.println("CSDN_LQR");
}

@AfterThrowing(value = "execution(* com.lqr..*(..))", throwing = "ex")
public void afterThrowing(Throwable ex) {
    System.out.println("记录日志");
}
```

可以看到，表达式是一样的，那要怎么重用这个表达式呢？这就需要用到@Pointcut注解了，@Pointcut注解是注解在一个空方法上的，如：

```java
@Pointcut("execution(* com.lqr..*(..))")
public void pointcut() {}
```

这时，"pointcut()"就等价于"execution(* com.lqr..*(..))"，那么上面的代码就可以这么改了：

```java
@Before("pointcut()")
public void before(JoinPoint point) {
    System.out.println("CSDN_LQR");
}

@AfterThrowing(value = "pointcut()", throwing = "ex")
public void afterThrowing(Throwable ex) {
    System.out.println("记录日志");
}
```
## 四、实战

经过上面的学习，下面是时候实战一下了，这里我们来一个简单的例子。

### 1、切点

这是界面上一个按钮的点击事件，就是一个简单的方法而已，我们拿它来试刀。
```java
public void test(View view) {
    System.out.println("Hello, I am CSDN_LQR");
}
```
### 2、切面类

要织入一段代码到目标类方法的前前后后，必须要有一个切面类，下面就是切面类的代码：
```java
@Aspect
public class TestAnnoAspect {

    @Pointcut("execution(* com.lqr.androidaopdemo.MainActivity.test(..))")
    public void pointcut() {

    }    

    @Before("pointcut()")
    public void before(JoinPoint point) {
        System.out.println("@Before");
    }

    @Around("pointcut()")
    public void around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("@Around");
    }

    @After("pointcut()")
    public void after(JoinPoint point) {
        System.out.println("@After");
    }

    @AfterReturning("pointcut()")
    public void afterReturning(JoinPoint point, Object returnValue) {
        System.out.println("@AfterReturning");
    }

    @AfterThrowing(value = "pointcut()", throwing = "ex")
    public void afterThrowing(Throwable ex) {
        System.out.println("@afterThrowing");
        System.out.println("ex = " + ex.getMessage());
    }
}
```
### 3、各通知的执行结果

先来试试看，这几个注解的执行结果如何。

### 4、方法耗时计算的实现
因为@Around是环绕通知，可以在切点的前后分别执行一些操作，AspectJ为了能肯定操作是在切点前还是在切点后，所以在@Around通知中需要手动执行joinPoint.proceed()来确定切点已经执行，故在joinPoint.proceed()之前的代码会在切点执行前执行，在joinPoint.proceed()之后的代码会切点执行后执行。于是，方法耗时计算的实现就是这么简单：
```java
@Around("pointcut()")
public void around(ProceedingJoinPoint joinPoint) throws Throwable {
    long beginTime = SystemClock.currentThreadTimeMillis();
    joinPoint.proceed();
    long endTime = SystemClock.currentThreadTimeMillis();
    long dx = endTime - beginTime;
    System.out.println("耗时：" + dx + "ms");
}
```
### 5、JoinPoint的作用
发现没有，上面所有的通知都会至少携带一个JointPoint参数，这个参数包含了切点的所有信息，下面就结合按钮的点击事件方法test()来解释joinPoint能获取到的方法信息有哪些：

```java
MethodSignature signature = (MethodSignature) joinPoint.getSignature();
String name = signature.getName(); // 方法名：test
Method method = signature.getMethod(); // 方法：public void com.lqr.androidaopdemo.MainActivity.test(android.view.View)
Class returnType = signature.getReturnType(); // 返回值类型：void
Class declaringType = signature.getDeclaringType(); // 方法所在类名：MainActivity
String[] parameterNames = signature.getParameterNames(); // 参数名：view
Class[] parameterTypes = signature.getParameterTypes(); // 参数类型：View
```
### 6、注解切点
前面的切点表达式结构是这样的：

execution(<修饰符模式>? <返回类型模式> <方法名模式>(<参数模式>) <异常模式>?)
但实际上，上面的切点表达式结构并不完整，应该是这样的：

execution(<@注解类型模式>? <修饰符模式>? <返回类型模式> <方法名模式>(<参数模式>) <异常模式>?)
这就意味着，切点可以用注解来标记了。

#### 1）自定义注解
如果用注解来标记切点，一般会使用自定义注解，方便我们拓展。
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestAnnoTrace {
    String value();
    int type();
}
```
- @Target(ElementType.METHOD)：表示该注解只能注解在方法上。如果想类和方法都可以用，那可以这么写@Target({ElementType.METHOD,ElementType.TYPE})，依此类推。
- @Retention(RetentionPolicy.RUNTIME)：表示该注解在程序运行时是可见的（还有SOURCE、CLASS分别指定注解对于那个级别是可见的，一般都是用RUNTIME）。

其中的value和type是自己拓展的属性，方便存储一些额外的信息。

#### 2）使用自定义注解标记切点
这个自定义注解只能注解在方法上（构造方法除外，构造方法也叫构造器，需要使用ElementType.CONSTRUCTOR），像平常使用其它注解一样使用它即可：

```java
@TestAnnoTrace(value = "lqr_test", type = 1)
public void test(View view) {
    System.out.println("Hello, I am CSDN_LQR");
}
```
#### 3）注解的切点表达式
既然用注解来标记切点，那么切点表达式肯定是有所不同的，要这么写：
```java
@Pointcut("execution(@com.lqr.androidaopdemo.TestAnnoTrace * *(..))")
public void pointcut() {}
```
> 切点表达式使用注解，一定是@+注解全路径，如：@com.lqr.androidaopdemo.TestAnnoTrace。


#### 4）获取注解属性值
上面在编写自定义注解时就声明了两个属性，分别是value和type，而且在使用该注解时也都为之赋值了，那怎么在通知中获取这两个属性值呢？还记得JoinPoint这个参数吧，它就可以获取到注解中的属性值，如下所示：
```java
MethodSignature signature = (MethodSignature) joinPoint.getSignature();
Method method = signature.getMethod();
// 通过Method对象得到切点上的注解
TestAnnoTrace annotation = method.getAnnotation(TestAnnoTrace.class);
String value = annotation.value();
int type = annotation.type();
```

