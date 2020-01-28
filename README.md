## 为什么使用AOP编程

项目开发过程中，可能会有这样的需求，需要我们在方法执行完成后，记录日志（后台开发中比较常见~），或是计算这个方法的执行时间，在不使用AOP的情况下，我们可以在方法最后调用另一个专门记录日志的方法，或是在方法体的首尾分别获取时间，然后通过计算时间差来计算整个方法执行所消耗的时间，这样也可以完成需求。那如果不只一个方法要这么玩怎么办？每个方法都写上一段相同的代码吗？后期处理逻辑变了要怎么办？最后老板说这功能不要了我们还得一个个删除？

很明显，这是不可能的，我们不仅仅是代码的搬运工，我们还是有思考能力的软件开发工程师。这么low的做法绝对不干，这种问题我们完全可以用AOP来解决，不就是在方法前和方法后插入一段代码吗？AOP分分钟搞定。

[![](https://jitpack.io/v/bamboolife/Aspectj.svg)](https://jitpack.io/#bamboolife/Aspectj)

## 如何使用
1. 在根build.gradle中添加
```java
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/" //发布插件的地址
        }
    }
    dependencies {
        classpath 'gradle.plugin.com.bamboo.plugin:aop-gradle-plugin:1.0.2' //引用自定义插件
    }
}
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }  //发布项目的地址
    }
}
```
2. 在module中添加
```java
apply plugin: 'com.bamboo.aspectjrt'


dependencies {
    implementation 'com.github.bamboolife.Aspectj:aop-api:1.0.2'
    annotationProcessor 'com.github.bamboolife.Aspectj:aop-compiler:1.0.2'
}
```
