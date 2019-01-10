package com.themejunky.module.notifyrating;

import android.app.Application;

import com.example.tj_notifyrating.Module_NotifyRating;

public class MainApplication extends Application {
    public Module_NotifyRating notifyRating;
    @Override
    public void onCreate() {
        super.onCreate();
    }
}
