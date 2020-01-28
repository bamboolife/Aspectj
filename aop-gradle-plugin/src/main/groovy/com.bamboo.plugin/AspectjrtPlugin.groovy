package com.bamboo.plugin

import org.aspectj.bridge.IMessage;
import org.gradle.api.Plugin;
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile;
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main


/**
 * 项目名称：aop-sample
 *
 * @Author bamboolife
 * 邮箱：core_it@163.com
 * 创建时间：2020-01-28 12:07
 * 描述：
 */
class AspectjrtPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {

        project.android.applicationVariants.all { variant ->
            if (!variant.buildType.isDebuggable()) {
                return
            }
            JavaCompile javaCompile = variant.getJavaCompileProvider().get()
            javaCompile.doLast {
                String[] args = ["-showWeaveInfo",
                                 "-1.8",
                                 "-inpath", javaCompile.destinationDir.toString(),
                                 "-aspectpath", javaCompile.classpath.asPath,
                                 "-d", javaCompile.destinationDir.toString(),
                                 "-classpath", javaCompile.classpath.asPath,
                                 "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)]
                MessageHandler handler = new MessageHandler(true)
                new Main().run(args, handler)

                def log = project.logger
                for (IMessage message : handler.getMessages(null, true)) {
                    switch (message.getKind()) {
                        case IMessage.ABORT:
                        case IMessage.ERROR:
                        case IMessage.FAIL:
                            log.error message.message, message.thrown
                            break;
                        case IMessage.WARNING:
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
    }
}
