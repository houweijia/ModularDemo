package com.jiehun.veigar.modulardemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.jiehun.veigar.api.core.ARouterLoadGroup;
import com.jiehun.veigar.api.core.ARouterLoadPath;
import com.jiehun.veigar.apt.ARouter$$Group$$order;
import com.jiehun.veigar.arouter_annotation.ARouter;
import com.jiehun.veigar.arouter_annotation.Parameter;
import com.jiehun.veigar.arouter_annotation.model.RouterBean;

import java.util.Map;

@ARouter(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {
    @Parameter()
    String name;
    @Parameter(name = "agex")
    int age;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    public void order(View view) {
//        try {
//            Class targetClass = Class.forName("com.jiehun.veigar.order.Order_MainActivity");
//            Intent intent = new Intent(this,targetClass);
//            intent.putExtra("name","simon");
//            startActivity(intent);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

        //最终集成化模式，所有子模块APT生成的类文件都会打包到apk中
        ARouterLoadGroup loadGroup = new ARouter$$Group$$order();
        Map<String, Class<? extends ARouterLoadPath>> groupMap = loadGroup.loadGroup();
        Class<? extends ARouterLoadPath> clazz = groupMap.get("order");
        try {
            ARouterLoadPath aRouterLoadPath = clazz.newInstance();
            Map<String, RouterBean> pathMap = aRouterLoadPath.loadPath();
            RouterBean routerBean = pathMap.get("/order/Order_MainActivity");
            if (routerBean != null) {
                Intent intent = new Intent(this, routerBean.getClazz());
                intent.putExtra("name", "simon");
                startActivity(intent);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    public void mine(View view) {
//        try {
//            Class targetClass = Class.forName("com.jiehun.veigar.personal.Personal_MainActivity");
//            Intent intent = new Intent(this,targetClass);
//            intent.putExtra("name","simon");
//            startActivity(intent);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

    }
}
