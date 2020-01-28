package com.bamboo.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bamboo.fastaop.FastAop;
import com.bamboo.sample.aop.AopLogin;
import com.bamboo.sample.aop.LogUtil;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: 11111111111111111");
                logPrintln();
            }
        });
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogin();
            }
        });
    }

    @AopLogin
    private void onLogin() {
        Log.i(TAG, "onLogin: ");
    }


    @LogUtil
    public void logPrintln() {
        Log.i(TAG, "intercept: 22222222222222");
    }


}
