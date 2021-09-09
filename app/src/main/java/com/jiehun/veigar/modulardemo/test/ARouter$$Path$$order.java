package com.jiehun.veigar.modulardemo.test;

import com.jiehun.veigar.api.core.ARouterLoadPath;
import com.jiehun.veigar.arouter_annotation.model.RouterBean;
import com.jiehun.veigar.order.Order_MainActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:模拟ARouter路由的组文件，对应的路径
 * @author: houwj
 * @date: 2019/7/29
 */
public class ARouter$$Path$$order implements ARouterLoadPath {
    @Override
    public Map<String, RouterBean> loadPath() {
        Map<String, RouterBean> pathMap = new HashMap<>();
        pathMap.put("/order/Order_MainActivity",
                RouterBean.create(RouterBean.Type.ACTIVITY,
                Order_MainActivity.class,
                "/order/Order_MainActivity",
                "order"));
        return pathMap;
    }
}
