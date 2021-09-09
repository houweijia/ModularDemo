package com.jiehun.veigar.modulardemo.test;

import com.jiehun.veigar.api.core.ARouterLoadGroup;
import com.jiehun.veigar.api.core.ARouterLoadPath;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 模拟路由组
 * @author: houwj
 * @date: 2019/7/29
 */
public class ARouter$$Group$$order implements ARouterLoadGroup {
    @Override
    public Map<String, Class<? extends ARouterLoadPath>> loadGroup() {
        Map<String,Class<? extends ARouterLoadPath>> groupMap = new HashMap<>();
        groupMap.put("order",ARouter$$Path$$order.class);
        return groupMap;
    }
}
