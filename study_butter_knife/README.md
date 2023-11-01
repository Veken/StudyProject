# ButterKnife框架原理
1、BindView、onClick、onLongClick等方式都是通过注解，拿到响应的资源id
2、通过继承java的AbstractProcessor类，该类只能在java的module中才能拿到，重写process()方法，拿到注解相关的数据
```
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    
    }
```
3、process方法详解
* 获取所有包含BindView注解的元素
```
Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindView.class);
```
* 需要对不同Activity中的注解进行分类，因为Set集合中包含了所有Activity中的注解
```
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
```
* 开始产生Java文件（这里面也有两种方式进行生成）
* 一种是自己拼接,类似于包名+类名+"_viewBinding"类似的
* 一种是通过JavaPoet进行生成，需要导包（implementation 'com.squareup:javapoet:1.11.1'）
* 通过TypeSpec.Builder生成类， MethodSpec.Builder生成构造方法，最重要的findViewById就在此
```
methodBuilder.addStatement("target." + variableName +"= (" + typeMirror + ")" + "target.findViewById(" + id + ");");
```

```
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
    //生成java文件
}
```
* 通过javax.annotation.processing包下的Filer类将生成的文件写入，在init()中进行初始化
```
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
    }

    //生产java文件
    JavaFile javaFile = JavaFile.builder(packageName,MainActivity_ViewBinder)
            .build();
    try {
        javaFile.writeTo(mFiler);
    } catch (IOException e) {
        e.printStackTrace();
    }
```
待遗留问题，因为新版AS对注解的资源id，判定为变量，这块可能需要像ButterKnife一样，修改为R2的资源，这块还得看一下ButterKnife的具体处理是怎么样的
```
@BindView(R.id.tv_butter_knife)
TextView tvButterKnife;
```