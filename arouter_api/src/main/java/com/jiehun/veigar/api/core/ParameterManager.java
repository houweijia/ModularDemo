package com.jiehun.veigar.api.core;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.LruCache;

/**
 * @description:参数Parameter加载管理器
 * @author: houwj
 * @date: 2019/8/6
 */
public class ParameterManager {
    private static ParameterManager instance;
    //Lru缓存 key 类名 value 参数Parameter加载接口
    private LruCache<String,ParameterLoad> cache;

    //APT生成的获取参数类文件，后缀名
    private static final String FILE_SUFFIX_NAME = "$$Parameter";

    //单例方式 全局唯一

    public static ParameterManager getInstance(){
        if(instance==null){
            synchronized (ParameterManager.class){
                if(instance==null){
                    instance = new ParameterManager();
                }
            }
        }
        return instance;
    }

    private ParameterManager() {
        //初始化 并赋值缓存中条目的最大值
        cache = new LruCache<>(163);
    }


    public void loadParameter(@NonNull Activity activity){
        String className = activity.getClass().getName();
        ParameterLoad iParameter = cache.get(className);

        //缓存中找不到

            try {
                if(iParameter == null) {
                    Class<?> clazz = Class.forName(className + FILE_SUFFIX_NAME);
                    iParameter= (ParameterLoad) clazz.newInstance();
                    cache.put(className,iParameter);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
}
