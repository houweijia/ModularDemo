package com.jiehun.veigar.modulardemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jiehun.veigar.arouter_annotation.ARouter;

@ARouter(path = "/app/Main2Activity")
public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }
}
