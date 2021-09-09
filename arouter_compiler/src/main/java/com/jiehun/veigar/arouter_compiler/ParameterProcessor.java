package com.jiehun.veigar.arouter_compiler;

import com.google.auto.service.AutoService;
import com.jiehun.veigar.arouter_annotation.Parameter;
import com.jiehun.veigar.arouter_compiler.factory.ParameterFactory;
import com.jiehun.veigar.arouter_compiler.utils.Constants;
import com.jiehun.veigar.arouter_compiler.utils.EmptyUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

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
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * @description:
 * @author: houwj
 * @date: 2019/8/1
 */

@AutoService(Processor.class)
//允许/支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes({Constants.PARAMETER_ANNOTATION_TYPES})

@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ParameterProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Types    typeUtils;
    private Messager messager;
    private Filer    filer;

    private Map<TypeElement, List<Element>> tempParameterMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if(!EmptyUtils.isEmpty(set)){
            //获取所有被Parameter注解 元素集合
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Parameter.class);
            if(!EmptyUtils.isEmpty(elements)){
                //用临时的Map存储，用来遍历生成代码
                valueOfParameterMap(elements);
                //生成类文件
                try {
                    createParameterFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private void createParameterFile() throws IOException {
        if(EmptyUtils.isEmpty(tempParameterMap)) return;
        //通过Element工具类，获取Parameter类型
        TypeElement  parameterType = elementUtils.getTypeElement(Constants.PARAMETER_LOAD);

        //参数体配置(Object target)
        ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT,Constants.PARAMETER_NAMR).build();
        for (Map.Entry<TypeElement, List<Element>> entry : tempParameterMap.entrySet()) {
            //Map集合中的key是类名，如MainActivity
            TypeElement typeElement = entry.getKey();
            //获取类名
            ClassName className = ClassName.get(typeElement);
            //方法体的构建
            ParameterFactory factory = new ParameterFactory.Builder(parameterSpec)
                    .setMessager(messager)
                    .setClassName(className)
                    .build();

            //添加方法体内容的第一行
            factory.addFirstStatement();

            //遍历方法体内容的第一行
            for (Element element : entry.getValue()) {
                factory.buildStatement(element);
            }

                    //最终生成
            String finalClassName = typeElement.getSimpleName() + Constants.PARAMETER_FILE_NAME;
            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成获取参数类文件：" +
                    className.packageName() + "." + finalClassName);

            // MainActivity$$Parameter
            JavaFile.builder(className.packageName(), // 包名
                    TypeSpec.classBuilder(finalClassName) // 类名
                            .addSuperinterface(ClassName.get(parameterType)) // 实现ParameterLoad接口
                            .addModifiers(Modifier.PUBLIC) // public修饰符
                            .addMethod(factory.build()) // 方法的构建（方法参数 + 方法体）
                            .build()) // 类构建完成
                    .build() // JavaFile构建完成
                    .writeTo(filer); // 文件生成器开始生成类文件

        }

    }

    private void valueOfParameterMap(Set<? extends Element> elements) {
        for (Element element : elements) {
            //注解的属性,父节点是类节点
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            //如果map集合中有这个类节点
            if(tempParameterMap.containsKey(typeElement)){
                tempParameterMap.get(typeElement).add(element);
            }else{
                List<Element> fields = new ArrayList<>();
                fields.add(element);
                tempParameterMap.put(typeElement,fields);
            }

        }
    }
}
