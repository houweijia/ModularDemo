package com.jiehun.veigar.order;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.jiehun.veigar.arouter_annotation.ARouter;
import com.jiehun.veigar.common.RecordPathManager;

@ARouter(path = "/order/Order_MainActivity")
public class Order_MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_activity_main);

        Intent intent = new Intent();
    }


    public void home(View view){
//        try {
//            Class targetClass = Class.forName("com.jiehun.veigar.modulardemo.MainActivity");
//            Intent intent = new Intent(this,targetClass);
//            intent.putExtra("name","simon");
//            startActivity(intent);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }


        Class<?> targetClass = RecordPathManager.getTargetClass("app", "MainActivity");
        if (targetClass == null) {
            Log.e("<<<", "获取targetClass为空");
        }
        Intent intent = new Intent(this, targetClass);
        intent.putExtra("name", "simon");
        startActivity(intent);
    }

    public void mine(View view){
//        try {
//            Class targetClass = Class.forName("com.jiehun.veigar.personal.Personal_MainActivity");
//            Intent intent = new Intent(this,targetClass);
//            intent.putExtra("name","simon");
//            startActivity(intent);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

        Class<?> targetClass = RecordPathManager.getTargetClass("personal", "Personal_MainActivity");
        if (targetClass == null) {
            Log.e("<<<", "获取targetClass为空");
        }
        Intent intent = new Intent(this, targetClass);
        intent.putExtra("name", "simon");
        startActivity(intent);
    }
}
