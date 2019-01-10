package com.themejunky.module.notifyrating;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.tj_notifyrating.Module_NotifyRating;

public class Main extends AppCompatActivity {
    public Module_NotifyRating notifyRating;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notifyRating = ((MainApplication)getApplication()).notifyRating;
        notifyRating = new Module_NotifyRating(this,true,Main2Activity.class,true);
        notifyRating.set_DebugMode("notifiTest");
        notifyRating.set_HoursAndRepeateTimes((10000),5,(1000*60*5));
        notifyRating.set_TextAndIconAndImage("Your opinion matters!","Please rate and review this keyboard theme!",R.drawable.icon_push_notification,R.drawable.banner_live_wallpapers3,R.layout.notification_type1,"alin");
        notifyRating.set_TextAndIcon("Your opinion matters!","Please rate and review this keyboard theme!",R.mipmap.ic_launcher);
        notifyRating.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    StatusBarNotification sad[] = notificationManager.getActiveNotifications();

                   // Log.d("4r43fa", String.valueOf(sad.length));

                }
            }
        },10000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    StatusBarNotification sad[] = notificationManager.getActiveNotifications();
                  //  Log.d("4r43fa", String.valueOf(sad.length));
                }
            }
        },20000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    StatusBarNotification sad[] = notificationManager.getActiveNotifications();
                  //  Log.d("4r43fa", String.valueOf(sad.length));
                }
            }
        },30000);

       boolean sadas =  getIntent().getBooleanExtra("alin",false);
        if(sadas){
            Log.d("wadada","A venit de pe luna");
        }else {
            Log.d("wadada","Nu primesc");
        }
    }
}
