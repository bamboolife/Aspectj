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

二、AspectJ的引入