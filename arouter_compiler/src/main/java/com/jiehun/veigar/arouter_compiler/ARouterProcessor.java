package com.jiehun.veigar.arouter_compiler;

import com.google.auto.service.AutoService;
import com.jiehun.veigar.arouter_annotation.ARouter;
import com.jiehun.veigar.arouter_annotation.model.RouterBean;
import com.jiehun.veigar.arouter_compiler.utils.Constants;
import com.jiehun.veigar.arouter_compiler.utils.EmptyUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * @description:
 * @author: houwj
 * @date: 2019/7/26
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes(Constants.AROUTER_ANNOTATION_TYPES)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedOptions({Constants.MODULE_NAME, Constants.APT_PACKAGE})
public class ARouterProcessor extends AbstractProcessor {

    private Elements                      elementsUtils;
    private Types                         typesUtils;
    private Messager                      messager;
    private Filer                         filer;
    private String                        packageNameForAPT;
    private String                        moduleName;
    private Map<String, List<RouterBean>> teamPathMap  = new HashMap<>();
    // 临时map存储，用来存放路由Group信息，生成路由组类文件时遍历
    // key:组名"app", value:类名"ARouter$$Path$$app.class"
    private Map<String, String>           tempGroupMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementsUtils = processingEnv.getElementUtils();
        typesUtils = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        Map<String, String> options = processingEnvironment.getOptions();
        if (!EmptyUtils.isEmpty(options)) {
            moduleName = options.get(Constants.MODULE_NAME);
            packageNameForAPT = options.get(Constants.APT_PACKAGE);

            messager.printMessage(Diagnostic.Kind.NOTE, "moduleName >>>" + moduleName);
            messager.printMessage(Diagnostic.Kind.NOTE, "packageNameForAPT >>>" + packageNameForAPT);
        }

        //必传参数判空(乱码问题 添加java控制台输出中文乱码)
        if (EmptyUtils.isEmpty(moduleName) || EmptyUtils.isEmpty(packageNameForAPT)) {
            throw new RuntimeException("注解处理器需要的参数moduleName或者packageName为空，请在对应build.gradle配置参数");
        }
    }


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //一旦有类上使用了ARouter注解
        if (!EmptyUtils.isEmpty(set)) {
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);
            if (!EmptyUtils.isEmpty(elements)) {
                //解析元素
                try {
                    parseElements(elements);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return true;
    }

    private void parseElements(Set<? extends Element> elements) throws IOException {
        //通过Element工具类 获取Activity类型
        TypeElement activityType = elementsUtils.getTypeElement(Constants.ACTIVITY);

        // 显示类信息（获取被注解节点，类节点）这里也叫自描述 Mirror
        TypeMirror activityMirror = activityType.asType();
        for (Element element : elements) {
            //获取每个元素的类信息
            TypeMirror elementMirror = element.asType();
            messager.printMessage(Diagnostic.Kind.NOTE, "遍历的元素信息为" + elementMirror.toString());

            //获取每个类上的ARouter注解，对应的path值
            ARouter aRouter = element.getAnnotation(ARouter.class);
            RouterBean bean = new RouterBean.Builder()
                    .setGroup(aRouter.group())
                    .setPath(aRouter.path())
                    .setElement(element)
                    .build();

            //高级判断 @ARouter注解仅仅只能用在类之上，并且是规定的Activity
            if (typesUtils.isSubtype(elementMirror, activityMirror)) {
                bean.setType(RouterBean.Type.ACTIVITY);
            } else {
                throw new RuntimeException("@ARouter注解目前仅用于Activity之上");
            }

            //赋值临时map存储以上信息，用来遍历时生成代码
            valueOfPathMap(bean);
        }

        //ARouterLoadGroup和ARouterLoadPath类型，用来生成类文件时实现接口
        TypeElement groupLoadType = elementsUtils.getTypeElement(Constants.AROUTE_GROUP);
        TypeElement pathLoadType = elementsUtils.getTypeElement(Constants.AROUTE_PATH);


        //1.生成路由的详细Path类文件，如ARouter$$Path$$app
        createPathFile(pathLoadType);
        //2.生成路由的组Group文件（没有path类文件，取不到）如ARouter$$Group$$app
        createGroupFile(groupLoadType, pathLoadType);
    }

    private void createGroupFile(TypeElement groupLoadType, TypeElement pathLoadType) throws IOException {

        //判断是否有需要生成的类文件
        if (EmptyUtils.isEmpty(tempGroupMap) || EmptyUtils.isEmpty(teamPathMap)) return;

        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),//Map
                ClassName.get(String.class),//Map<String,
                //第二个参数：Class<? extends ARouterLoadPath>
                //某某Class是否属于ARouterLoadPath接口的实现类
                ParameterizedTypeName.get(ClassName.get(Class.class),
                WildcardTypeName.subtypeOf(ClassName.get(pathLoadType)))
        );

        //方法配置 public Map<String, Class<? extends ARouterLoadPath>> loadGroup() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.GROUP_METHOD_NAME)//方法名
                .addAnnotation(Override.class)//重写注解
                .addModifiers(Modifier.PUBLIC)//public修饰符
                .returns(methodReturns);//返回值

        //遍历之前 Map<String, Class<? extends ARouterLoadPath>> groupMap = new HashMap<>();
        methodBuilder.addStatement("$T<$T,$T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathLoadType))),
                Constants.GROUP_PARAMETER_NAME,
                HashMap.class);

        //方法内容配置
        for (Map.Entry<String, String> entry : tempGroupMap.entrySet()) {
            // 类似String.format("hello %s net163 %d", "net", 163)通配符
            // groupMap.put("main", ARouter$$Path$$app.class);
            methodBuilder.addStatement("$N.put($S,$T.class)",
                    Constants.GROUP_PARAMETER_NAME,
                    entry.getKey(),
                    ClassName.get(packageNameForAPT, entry.getValue()));
        }

        //遍历之后:return groupMap
        methodBuilder.addStatement("return $N", Constants.GROUP_PARAMETER_NAME);

        //最终生成的类文件名
        String finalClassName = Constants.GROUP_FILE_NAME + moduleName;
        messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由组Group类文件：" +
                packageNameForAPT + "." + finalClassName);

        //生成类文件：ARouter$$Group$$app
        JavaFile.builder(packageNameForAPT,//包名
                TypeSpec.classBuilder(finalClassName)//类名
                        .addSuperinterface(ClassName.get(groupLoadType))//实现ARouterLoadGroup
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(methodBuilder.build())
                        .build())
                .build()
                .writeTo(filer);
    }

    /**
     * 生成路由组Group对应详细Path 如：ARouter$$Path$$app
     *
     * @param pathLoadType
     */
    private void createPathFile(TypeElement pathLoadType) throws IOException {
        if (EmptyUtils.isEmpty(teamPathMap)) return;

        //方法的返回值Map<String,RouterBean>
        ParameterizedTypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouterBean.class));
        //遍历分组 每一个分组创建一个路径类文件 如ARouter$$Path$$app
        for (Map.Entry<String, List<RouterBean>> entry : teamPathMap.entrySet()) {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.PATH_METHOD_NAME)
                    .addAnnotation(Override.class)//重写注解
                    .addModifiers(Modifier.PUBLIC)//public 修饰符
                    .returns(methodReturns);

            //不循环部分  Map<String, RouterBean> pathMap = new HashMap<>();
            methodBuilder.addStatement("$T<$T,$T> $N = new $T<>()",
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(RouterBean.class),
                    Constants.PATH_PARAMETER_NAME,
                    HashMap.class);

            //app/MainActivity, app/.....
            List<RouterBean> pathList = entry.getValue();
            for (RouterBean bean : pathList) {
                //方法内容的循环部分
                /**
                 *         pathMap.put("/order/Order_MainActivity",
                 *                 RouterBean.create(RouterBean.Type.ACTIVITY,
                 *                 Order_MainActivity.class,
                 *                 "/order/Order_MainActivity",
                 *                 "order"));
                 *         return pathMap;
                 */

                methodBuilder.addStatement(
                        "$N.put($S,$T.create($T.$L,$T.class,$S,$S))",
                        Constants.PATH_PARAMETER_NAME,
                        bean.getPath(),
                        ClassName.get(RouterBean.class),
                        ClassName.get(RouterBean.Type.class),
                        bean.getType(),//枚举Activity
                        ClassName.get((TypeElement) bean.getElement()),//MainActivity.class
                        bean.getPath(),// "/app/MainActivity"
                        bean.getGroup());//app
            }

            methodBuilder.addStatement("return $N", Constants.PATH_PARAMETER_NAME);

            //生成类文件

            String finalClassName = Constants.PATH_FILE_NAME + entry.getKey();
            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Path类文件为：" +
                    packageNameForAPT + "." + finalClassName);

            JavaFile.builder(packageNameForAPT, TypeSpec.classBuilder(finalClassName)
                    .addSuperinterface(ClassName.get(pathLoadType))//实现接口
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(methodBuilder.build())//方法体
                    .build())
                    .build()
                    .writeTo(filer);//类构建完成

            //赋值
            tempGroupMap.put(entry.getKey(), finalClassName);

        }
    }

    /**
     * 赋值临时map存储，用来存放路由组Group对应的详细Path类对象，生成路由路径文件时遍历
     *
     * @param bean
     */
    private void valueOfPathMap(RouterBean bean) {
        if (checkRouterPath(bean)) {
            messager.printMessage(Diagnostic.Kind.NOTE, "RouterBean >>>" + bean.toString());

            //开始赋值
            List<RouterBean> routerBeans = teamPathMap.get(bean.getGroup());

            //如果map中找不到key
            if (EmptyUtils.isEmpty(routerBeans)) {
                routerBeans = new ArrayList<>();
                routerBeans.add(bean);
                teamPathMap.put(bean.getGroup(), routerBeans);
            } else {//找到了key 直接加入临时集合
                routerBeans.add(bean);
            }
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, "@ARouter注解未按规范,如：/app/MainActivity");
        }


    }

    private boolean checkRouterPath(RouterBean bean) {
        String group = bean.getGroup();
        String path = bean.getPath();

        //ARouter注解的path值必须要以/开头（模仿阿里ARouter路由架构）

        if (EmptyUtils.isEmpty(path) || !path.startsWith("/")) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范，如：/app/MainActivity");
            return false;
        }

        //比如开发者代码为path = "/MainActivity"
        if (path.lastIndexOf("/") == 0) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范，如：/app/MainActivity");
            return false;
        }

        //比如开发者代码为path = "/MainActivity/MainActivity/MainActivity"
        String finalGroup = path.substring(1, path.indexOf("/", 1));
        if (finalGroup.contains("/")) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范，如：/app/MainActivity");
            return false;
        }

        //ARouter注解中有group赋值
        if (EmptyUtils.isEmpty(group) && group.equals(moduleName)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的group值必须和当前子模块名相同");
            return false;
        } else {
            bean.setGroup(finalGroup);
        }
        return true;
    }
}
