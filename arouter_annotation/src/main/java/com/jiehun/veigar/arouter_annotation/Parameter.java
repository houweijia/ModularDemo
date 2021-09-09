package com.jiehun.veigar.arouter_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description:
 * @author: houwj
 * @date: 2019/8/1
 */
@Target(ElementType.FIELD)//注解作用在属性之上
@Retention(RetentionPolicy.CLASS)
public @interface Parameter {

    //不填写name的注解值表示该属性名就是key 填写了就作用注解值作为key
    //从getIntent()方法中获取传递参数值
    String name() default "";
}
