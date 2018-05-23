package com.h.demo;

import android.app.Application;

import com.dump.utils.Utils;

/**
 * Created by huan on 2018/5/12.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}
