package com.example.lin_new_project_kotlin_ai;

import android.app.Application;

import com.example.lin_new_project_kotlin_ai.fun.MmkvUtils;
import com.example.lin_new_project_kotlin_ai.room.DatabaseManager;
import com.tencent.mmkv.MMKV;

public class MyApplication extends Application {
    public static final int nfc = 1001;
    public volatile static MyApplication instance;
    public MmkvUtils mmkvUtils;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        MMKV.initialize(this);
    }
    public static MyApplication getInstance() {
        if (instance == null) {
            synchronized (MyApplication.class) {
                if (instance == null) {
                    instance = new MyApplication();
                }
            }
        }
        return instance;
    }
    public static MmkvUtils mmkvUtils() {
        if (getInstance().mmkvUtils == null) {
            getInstance().mmkvUtils = new MmkvUtils();
        }
        return getInstance().mmkvUtils;
    }
    public static DatabaseManager mDatabaseManager() {
        return DatabaseManager.getInstance(getInstance());
    }
}
