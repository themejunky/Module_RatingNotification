package com.themejunky.module.notifyrating;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.tj_notifyrating.Module_NotifyRating;

public class Main extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Module_NotifyRating notifyRating = new Module_NotifyRating(this,true,Main.class,getPackageName());
        notifyRating.set_DebugMode("notifiTest");
        notifyRating.set_HoursAndRepeateTimes((5000),1,(1000*60*5));
        notifyRating.set_TextAndIconAndImage("Your opinion matters!","Please rate and review this keyboard theme!",R.drawable.icon_push_notification,R.drawable.banner_live_wallpapers3,R.layout.notification_type1);
        notifyRating.set_TextAndIcon("Your opinion matters!","Please rate and review this keyboard theme!",R.mipmap.ic_launcher);
        notifyRating.start();
    }
}
