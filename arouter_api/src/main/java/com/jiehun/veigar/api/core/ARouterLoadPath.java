package com.jiehun.veigar.api.core;

import com.jiehun.veigar.arouter_annotation.model.RouterBean;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 路由组Group对应的详细Path加载数据接口
 * 比如：app分组对应有哪些类需要加载
 * @author: houwj
 * @date: 2019/7/26
 */
public interface ARouterLoadPath {
    Map<String, RouterBean> loadPath();
}
