package com.jiehun.veigar.api.core;

/**
 * @description: 参数Parameter加载接口
 * @author: houwj
 * @date: 2019/8/1
 */
public interface ParameterLoad {
    /**
     * 目标对象，属性名 = target.getIntent().属性类型("注解值or属性名");完整赋值
     * @param target 目标对象，如MainActivity(中的某些属性)
     */
    void loadParameter(Object target);
}
