package com.bamboo.fastaop;


import com.bamboo.aspectj.FastAop;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class FastAopProcessor extends AbstractProcessor {
    private Filer mFiler;
    private Elements mElementUtils;
    private Messager mMessager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler=processingEnvironment.getFiler();
        mElementUtils=processingEnvironment.getElementUtils();
        mMessager=processingEnvironment.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes=new LinkedHashSet<>();
        annotationTypes.add(FastAop.class.getCanonicalName());
        return annotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 遍历所有被注解了@FastAop的元素(可能是类、接口、方法)
        for (Element annotatedElement:roundEnvironment.getElementsAnnotatedWith(FastAop.class)) {
            TypeElement typeElement= (TypeElement) annotatedElement;
            PackageElement packageElement= (PackageElement) annotatedElement.getEnclosingElement();
          //  mMessager.printMessage(Diagnostic.Kind.ERROR,packageElement.toString());

            AnnotationSpec pointcut = AnnotationSpec.builder(ClassName.get("org.aspectj.lang.annotation","Pointcut"))
                    .addMember("value", "\"execution(@" + typeElement.getQualifiedName() + " * *(..))\"").build();
            MethodSpec pointcutMethod = MethodSpec.methodBuilder("pointcut" + typeElement.getSimpleName())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addAnnotation(pointcut)
                    .build();
            AnnotationSpec around = AnnotationSpec.builder(ClassName.get("org.aspectj.lang.annotation","Around"))
                    .addMember("value", "\"" + pointcutMethod.name + "()\"").build();
            MethodSpec aroundMethod = MethodSpec.methodBuilder("around" + typeElement.getSimpleName())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addParameter(ClassName.get("org.aspectj.lang","ProceedingJoinPoint"), "joinPoint")
                    .addAnnotation(around)
                    .addStatement("if (!(joinPoint.getSignature() instanceof $T)){\n\tthrow new $T(\"This annotation can only be used on Methods\");\n}"
                            ,ClassName.get("org.aspectj.lang.reflect","MethodSignature"),IllegalStateException.class)
                    .addStatement("MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();")
                    .addStatement(typeElement.getSimpleName()+" annotation = methodSignature.getMethod().getAnnotation("+typeElement.getSimpleName()+".class);")
                    .addStatement("$T.interceptor("+typeElement.getSimpleName()+".class,joinPoint)",ClassName.get("com.bamboo.fastaop","FastAop"))
                    .build();

            TypeSpec OkAspectj = TypeSpec.classBuilder(typeElement.getSimpleName().toString() + "_Aspectj")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(pointcutMethod)
                    .addMethod(aroundMethod)
                    .addAnnotation(AnnotationSpec.builder(ClassName.get("org.aspectj.lang.annotation","Aspect")).build())
                    .build();
            JavaFile javaFile = JavaFile.builder(packageElement.getQualifiedName().toString(), OkAspectj).build();
            try {
                javaFile.writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
