# Aspectj
切面编程
##如何使用
1. 在根build.gradle中添加
```java
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        classpath 'gradle.plugin.com.bamboo.plugin:aop-gradle-plugin:1.0.2'
    }
}
allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://jitpack.io' }
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
