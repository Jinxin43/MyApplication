package com.example.event;

import android.app.Application;

import androidx.multidex.MultiDex;

import com.example.event.utils.CrashCatchHandler;

/**
 * Created by Dingtu2 on 2018/9/11.
 */

public class PatrolApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CrashCatchHandler crashCatchHandler = CrashCatchHandler.getInstance();//获得单例
        crashCatchHandler.init(getApplicationContext());//初始化,传入context
        MultiDex.install(this);
    }
}
