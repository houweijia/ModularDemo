package com.jiehun.veigar.modulardemo;

import com.jiehun.veigar.api.core.ParameterLoad;

/**
 * @description:
 * @author: houwj
 * @date: 2019/8/1
 */
public class XActivity$$Parameter implements ParameterLoad {
    @Override
    public void loadParameter(Object target) {
        MainActivity t = (MainActivity) target;
        t.name = t.getIntent().getStringExtra("name");
        t.age = t.getIntent().getIntExtra("age",t.age);
    }
}
