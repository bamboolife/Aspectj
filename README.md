# Aspectj
切面编程
[![](https://jitpack.io/v/bamboolife/Aspectj.svg)](https://jitpack.io/#bamboolife/Aspectj)
##如何使用
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
