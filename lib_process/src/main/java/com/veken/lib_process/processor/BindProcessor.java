package com.veken.lib_process.processor;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.veken.lib_process.annotaions.BindView;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Veken on 2023/10/30 16:28
 * ButterKnife
 * @desc
 */
@AutoService(Process.class)
public class BindProcessor extends AbstractProcessor {

    //写文件的对象
    private Filer mFiler;
    private Elements mElementUtils;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
    }

    /**
     * 注解处理器支持的Java版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 注解处理器支持的注解名称
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new HashSet<>();
        annotationTypes.add(BindView.class.getCanonicalName());
        return annotationTypes;
    }

    /**
     * @param roundEnv
     *
     * package practice.lxn.cn.androidpractice.pojo; // PackageElement
     *   public class Book implements Parcelable{ // TypeElement
     *      private int bookId; // VariableElement
     *      private String bookName; // VariableElement
     *      public int getBookId() { // ExecutableElement
     *          return bookId;
     *      }
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 获取所有包含BindView注解的元素
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindView.class);
        // 需要对不同Activity中的注解进行分类，因为Set集合中包含了所有Activity中的注解
        Map<String, List<VariableElement>> activityElementMap = new HashMap<>();
        for (Element element : elements) {
            VariableElement variableElement = (VariableElement) element;
            //获取当前元素对应的Activity名称
            String activityName = getActivityName(variableElement);
            List<VariableElement> elementList = activityElementMap.get(activityName);
            if (elementList == null) {
                elementList = new ArrayList<>();
                //将Activity名称和它对应的元素集合存放到一起
                activityElementMap.put(activityName,elementList);
            }
            elementList.add(variableElement);
        }

        // 开始产生Java文件
        for (String activityName : activityElementMap.keySet()) {
            // 获取Activity对应的带注解的成员
            List<VariableElement> elementList = activityElementMap.get(activityName);
            // 获取包名
            String packageName = getPackageName(elementList.get(0));
            // 获取最后生成的文件的名称package practice.lxn.cn.testapp.MainActivity_ViewBinder;
//            String viewBinderName = activityName + "_ViewBinder";
            /* 需要生成文件的格式
            package practice.lxn.cn.testapp;
            import practice.lxn.cn.testapp.ViewBinder
            public class MainActivity_ViewBinder implements ViewBinder<MainActivity> {
                @Override
                public void bind(MainActivity target) {
                    target.btn = (Button)target.findViewById(1231123423432);
                }
            }*/
            //===========================================================================
           /* Writer writer;
            //MainActivity_ViewBinder
            String simpleName = elementList.get(0).getEnclosingElement().getSimpleName().toString() + "_ViewBinder";
            try {
                // 方式一：通过原生的JavaFileObject拼接
                JavaFileObject javaFileObject = mFiler.createSourceFile(viewBinderName);
                writer = javaFileObject.openWriter();
                writer.write("package " + packageName +";");
                writer.write("\n");
                writer.write("import " + packageName + ".ViewBinder;");
                writer.write("\n");
                writer.write("public class " + simpleName + " implements ViewBinder<" + activityName + "> {");
                writer.write("\n");
                writer.write("public void bind(" + activityName + " target) {");
                writer.write("\n");
                for (VariableElement element : elementList) {
                    String variableName = element.getSimpleName().toString();
                    TypeMirror typeMirror = element.asType();
                    int id = element.getAnnotation(BindView.class).value();
                    writer.write("target." + variableName + " = (" + typeMirror + ")target.findViewById(" + id + ");");
                    writer.write("\n");
                    writer.write("}");
                    writer.write("\n");
                    writer.write("}");

                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                writer.close(); // 写完需要关闭
             }*/
            //方式二：通过JavaPoet提供的API
              /* 需要生成文件的格式
            package practice.lxn.cn.testapp;
            import practice.lxn.cn.testapp.ViewBinder
            public class MainActivity_ViewBinder implements ViewBinder<MainActivity> {
                @Override
                public void bind(MainActivity target) {
                    target.btn = (Button)target.findViewById(1231123423432);
                }
            }*/
            String simpleName = elementList.get(0).getEnclosingElement().getSimpleName().toString();
            ClassName viewBinderName = ClassName.get(packageName, simpleName);
            ClassName activityClassName = ClassName.bestGuess(simpleName);
            //创建类
            TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(activityClassName + "_ViewBinding")
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ParameterizedTypeName.get(viewBinderName,activityClassName));
            //创造构建方法
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(TypeVariableName.get(activityName),"target")
                    .returns(TypeName.VOID);
            for (VariableElement element : elementList) {
                String variableName = element.getSimpleName().toString();
                TypeMirror typeMirror = element.asType();
                int id = element.getAnnotation(BindView.class).value();
                methodBuilder.addStatement("target." + variableName +"= (" + typeMirror + ")" + "target.findViewById(" + id + ");");
            }
            MethodSpec bind = methodBuilder.build();
            TypeSpec MainActivity_ViewBinder = typeBuilder.addMethod(bind).build();
            //生产java文件
            JavaFile javaFile = JavaFile.builder(packageName,MainActivity_ViewBinder)
                    .build();
            try {
                javaFile.writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取包名
     */
    private String getPackageName(VariableElement variableElement) {
        TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
        PackageElement packageElement = mElementUtils.getPackageOf(typeElement);
        return packageElement.getQualifiedName().toString();
    }

    /**
     * 获取Activity名称
     */
    private String getActivityName(VariableElement variableElement) {
        TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
        String packageName = getPackageName(variableElement);
        // package practice.lxn.cn.testapp.MainActivity
        return packageName + "." + typeElement.getSimpleName().toString();
    }

}
