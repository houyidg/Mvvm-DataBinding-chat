package com.example.administrator.app;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2017/5/9.
 */

public class AppApplication extends Application {
    public static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
    }
}
