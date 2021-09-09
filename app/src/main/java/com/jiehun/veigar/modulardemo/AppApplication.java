package com.jiehun.veigar.modulardemo;

import com.jiehun.veigar.common.RecordPathManager;
import com.jiehun.veigar.common.base.BaseApplication;
import com.jiehun.veigar.order.Order_MainActivity;
import com.jiehun.veigar.personal.Personal_MainActivity;

/**
 * @description:
 * @author: houwj
 * @date: 2019/7/24
 */
public class AppApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        RecordPathManager.joinGroup("app", "MainActivity", MainActivity.class);
        RecordPathManager.joinGroup("order", "Order_MainActivity", Order_MainActivity.class);
        RecordPathManager.joinGroup("personal", "Personal_MainActivity", Personal_MainActivity.class);
    }
}
