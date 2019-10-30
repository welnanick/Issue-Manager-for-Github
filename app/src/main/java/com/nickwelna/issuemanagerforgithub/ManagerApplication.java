package com.nickwelna.issuemanagerforgithub;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

public final class ManagerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
