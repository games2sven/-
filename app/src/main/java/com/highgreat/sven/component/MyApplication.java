package com.highgreat.sven.component;

import android.app.Application;

import com.highgreat.sven.router_core.HGRouter;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            HGRouter.init(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
