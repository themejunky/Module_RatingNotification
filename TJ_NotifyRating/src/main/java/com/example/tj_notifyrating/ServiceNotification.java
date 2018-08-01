package com.example.tj_notifyrating;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.tj_notifyrating.utils.Stuff;

import java.net.URISyntaxException;
import java.util.ArrayList;

import static com.example.tj_notifyrating.utils.Constants.DEFAULT_DEBUG_MODE;
import static com.example.tj_notifyrating.utils.Constants.DEFAULT_DEBUG_MODE_TAG;
import static com.example.tj_notifyrating.utils.Constants.DEFAULT_DELAY_BETWEEN_ATTEMPTS_INTERNET_NOT_SURE;
import static com.example.tj_notifyrating.utils.Constants.DEFAULT_DELAY_BETWEEN_NOTIFICATIONS;
import static com.example.tj_notifyrating.utils.Constants.DEFAULT_NR_OF_NOTIFICATIONS;
import module.themejunky.com.tj_gae.Module_GoogleAnalyticsEvents;


public class ServiceNotification extends Service {

    private Module_GoogleAnalyticsEvents mGae;
    /* if it's set it contains Google Analytics Id for sending events */
    private String GaePropertyId;
    /* Main category Event for normal events */
    private String categoryNormalEvent;
    /* Main category Event for crash events */
    private String categoryCrashEvent;
    /* number of times notification was send */
    private int nrTimesNotficationAppear;
    /* number of time notification should appear one after one by millisSecondsDelay */
    private int nrOfNotifications;
    /* milliseconds delay between onCreate() time and first notification*/
    private int millisSecondsDelay;
    /* milliseconds delay between new attempts when internet connection is not sure*/
    private int millisSecondsAttempts;
    /* if set to "true" Log.d will print */
    private boolean debugMode;
    /* debug mode TAG for Log.d */
    private String debugModeTag = DEFAULT_DEBUG_MODE_TAG;
    /* if is set to "false" => no notification and no service alarm*/
    private Boolean pushFlag;
    /* waiting to start ....*/
    private Intent onNotificationTapIntent;
    /* ArrayList that will colect all logs and prind at the end */
    /* utils */
    private Stuff myStuff;

    private String notificationTitle,notificationSubtitle;
    private String notificationImageTitle,notificationImageSubtitle;
    private int notificationIcon;
    private int notificationSmallIcon,notificationBigIcon,notificationLayout;

    private int nextWakeUp;
    private String typeInternetLive = "none";

    private ArrayList<String> logsCollector;
    SharedPreferences.Editor sharedEdit;
    private boolean isnotificationImage;
    private String valueIntent;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        try {
            start_service_step_1();
        } catch (Exception ignored) {
        }



        try {
            start_service_step_2();
        } catch (Exception e) {
            myStuff.showErrorLog(debugMode, debugModeTag, "* NOTIFY_RATING_LIB : (service-0) : "+e.getMessage());
        }



        try {
            start_service_running_logic(intent);
        } catch (Exception e) {
            myStuff.showErrorLog(debugMode, debugModeTag, "* NOTIFY_RATING_LIB : (service-1) : "+e.getMessage());
            if (mGae!=null) { mGae.getEvents(categoryCrashEvent,"NOTIFY_RATING_LIB (service-1)","Error : "+e.getMessage()); }
        }



        return Service.START_NOT_STICKY;
    }


    /**
     * Gather information from pref and initialize ...things
     */
    private void start_service_step_1() {
        sharedEdit = PreferenceManager.getDefaultSharedPreferences(this).edit();
        myStuff = new Stuff();
        getSettingsFromPref();
        logsCollector = new ArrayList<>();
    }


    /**
     * If propertyId provided (GAE) try initialization
     */
    private void start_service_step_2() {
        if (GaePropertyId != null) {
            mGae = Module_GoogleAnalyticsEvents.getInstance(this,GaePropertyId);
            if (debugMode) {
                mGae.setDebug(debugModeTag);
            }
        }
    }

    /**
     * By now service is upRunning ...start logic
     */
    private void start_service_running_logic(Intent intent) {
        if (intent.getExtras().getString(getResources().getString(R.string.intent_key_first_launch)) != null) {
            if (mGae!=null) { mGae.getEvents(categoryNormalEvent,"NOTIFY_RATING_LIB","First Launch | Set first alarm"); }
            stopSelf();
        } else {

            if (mGae!=null) { mGae.getEvents(categoryNormalEvent,"NOTIFY_RATING_LIB","Service wakeUp by Alarm"); }

            logsCollector.add("*");
            logsCollector.add("------- Notification no: " + nrTimesNotficationAppear);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    notificationLogic(ServiceNotification.this, onNotificationTapIntent);
                }
            }).start();
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        try {
            onDestroyService();
        } catch (Exception e) {
            myStuff.showErrorLog(debugMode, debugModeTag, "* NOTIFY_RATING_LIB (service-2) : "+e.getMessage());
            if (mGae!=null) { mGae.getEvents(categoryCrashEvent,"NOTIFY_RATING_LIB (start-2)","Error : "+e.getMessage()); }
        }
    }

    private void onDestroyService() {
        if (pushFlag) {

            if (nextWakeUp==0) {
                nextWakeUp = millisSecondsDelay; //corection
            }

            if (mGae!=null) { mGae.getEvents(categoryNormalEvent,"NOTIFY_RATING_LIB","Service will wake up in "+nextWakeUp+" millis"); }

            logsCollector.add("* Service wake up : true | Next wake up in : "+nextWakeUp+" millis | "+myStuff.millisToTime(nextWakeUp)+"  | "+myStuff.convertDate(((System.currentTimeMillis()+nextWakeUp))));

            sharedEdit.putLong(getResources().getString(R.string.push_notification_wakeup_time), System.currentTimeMillis()+nextWakeUp);
            sharedEdit.commit();

            ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).set(0, System.currentTimeMillis() + nextWakeUp, PendingIntent.getService(this, 0, new Intent(getApplicationContext(), ServiceNotification.class), 0));
        } else {
            logsCollector.add("* Service wake up : false");
            if (mGae!=null) { mGae.getEvents(categoryNormalEvent,"NOTIFY_RATING_LIB","Service will not wake up"); }
        }

        myStuff.showLogs(debugMode,debugModeTag,logsCollector);
    }

    private void notificationLogic(Context context,Intent intent) {

        Boolean sercureFlag=false;
        Boolean internetConnectionLive = myStuff.checkForInternetConnection(this);
        Boolean internetConnection = myStuff.haveNetworkConnection(this);

        if (nrTimesNotficationAppear<=nrOfNotifications) {
             sercureFlag = true;
        }

        logsCollector.add("* PushFlag : "+pushFlag);
        logsCollector.add("* SecureFlag : "+sercureFlag +" ("+nrTimesNotficationAppear+"<="+nrOfNotifications+")");
        logsCollector.add("* InternetConection : "+internetConnection);
        logsCollector.add("* InternetConnectionLive : "+internetConnectionLive+" ("+typeInternetLive+")");
        logsCollector.add("*");

        if (internetConnection) {
            if (pushFlag && internetConnectionLive && sercureFlag) {

                if (mGae!=null) { mGae.getEvents(categoryNormalEvent,"NOTIFY_RATING_LIB No. "+nrTimesNotficationAppear,"Send : true | ALL GOOD | PF : "+pushFlag+" | SF "+(nrTimesNotficationAppear<=nrOfNotifications)+" | IC : "+internetConnection+" | ICL : "+internetConnectionLive); }

                // all good normal shit
                logsCollector.add("* Notification send : true");
                logsCollector.add("* Next wake up set : "+millisSecondsDelay+" (millis)");
                nextWakeUp = millisSecondsDelay;

                nrTimesNotficationAppear++;
                sharedEdit.putInt(getResources().getString(R.string.pref_key_timeNotificationAppears), nrTimesNotficationAppear);
                sharedEdit.apply();

                if(isnotificationImage){
                    sendPushNotificationImage(context, intent);
                }else {
                    sendPushNotification(context, intent);
                }

            } else if (pushFlag && !internetConnectionLive && sercureFlag) {

                if (mGae!=null) { mGae.getEvents(categoryNormalEvent,"NOTIFY_RATING_LIB No. "+nrTimesNotficationAppear,"Send : false | NO LIVE INTERNET | PF : "+pushFlag+" | SF "+(nrTimesNotficationAppear<=nrOfNotifications)+" | IC : "+internetConnection+" | ICL : "+internetConnectionLive); }

                // all good but not LiveInternet
                logsCollector.add("* Notification send : false");
                logsCollector.add("* Next wake up set : "+millisSecondsAttempts+" (millis)");
                nextWakeUp = millisSecondsAttempts;
            } else {

                if (mGae!=null) { mGae.getEvents(categoryNormalEvent,"NOTIFY_RATING_LIB No. "+nrTimesNotficationAppear,"Send : false | >2 FALSE | PF : "+pushFlag+" | SF "+(nrTimesNotficationAppear<=nrOfNotifications)+" | IC : "+internetConnection+" | ICL : "+internetConnectionLive); }

                // more stuff not good : Shut Down Service
                logsCollector.add("* Notification send : false");
                sharedEdit.putBoolean(getResources().getString(R.string.push_notification_flag), false);
                sharedEdit.putBoolean(getResources().getString(R.string.push_notification_idle), false);
                pushFlag=false;
                sharedEdit.apply();
            }
        } else {

            if (mGae!=null) { mGae.getEvents(categoryNormalEvent,"NOTIFY_RATING_LIB No. "+nrTimesNotficationAppear,"Send : false | NO CONNECTION - BROADCAST | PF : "+pushFlag+" | SF "+(nrTimesNotficationAppear<=nrOfNotifications)+" | IC : "+internetConnection+" | ICL : "+internetConnectionLive); }

            logsCollector.add("* Notification send : false (will wakeUp by broadcast)");
            sharedEdit.putBoolean(getResources().getString(R.string.push_notification_flag), false);
            sharedEdit.putBoolean(getResources().getString(R.string.push_notification_idle), true);
            pushFlag=false;
            sharedEdit.apply();
        }
        stopSelf();
    }


    private void sendPushNotification(Context context,Intent myIntent) {

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, myIntent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(notificationIcon)
                .setContentTitle(notificationTitle)
                .setWhen(System.currentTimeMillis())
                .setContentText(notificationSubtitle)
                .setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        Notification note = mBuilder.build();
        note.defaults |= Notification.DEFAULT_LIGHTS;

        mNotificationManager.notify(12345, note);
    }


    public void sendPushNotificationImage(Context context, Intent intent) {
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                notificationBigIcon);
        int notificationId = 333;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        RemoteViews remoteViews = new RemoteViews(getPackageName(),notificationLayout);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(notificationSmallIcon)
                .setContentTitle(notificationImageTitle)
                .setContentText(notificationImageSubtitle)
                .setContent(remoteViews)
                .setLargeIcon(icon)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(icon)
                        .bigLargeIcon(null));

        Log.d("wadada","sendPushNotificationImage :" +valueIntent);
        intent.putExtra(valueIntent,true);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        final Notification notification = mBuilder.build();
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(notificationId, mBuilder.build());
    }

    private void getSettingsFromPref() {
        Log.d("asjkdhgfasdf","getSettingsFromPref");
        try {
            SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);

            nrTimesNotficationAppear = shared.getInt(getResources().getString(R.string.pref_key_timeNotificationAppears), 1);
            nrOfNotifications = shared.getInt(getResources().getString(R.string.pref_key_nrTimes), DEFAULT_NR_OF_NOTIFICATIONS);
            millisSecondsDelay = shared.getInt(getResources().getString(R.string.pref_key_millisSecondsDelayNotification), DEFAULT_DELAY_BETWEEN_NOTIFICATIONS);
            millisSecondsAttempts = shared.getInt(getResources().getString(R.string.pref_key_millisSecondsDelayAttempts), DEFAULT_DELAY_BETWEEN_ATTEMPTS_INTERNET_NOT_SURE);
            debugMode = shared.getBoolean(getResources().getString(R.string.pref_key_debug), DEFAULT_DEBUG_MODE);
            debugModeTag = shared.getString(getResources().getString(R.string.pref_key_debug_tag), DEFAULT_DEBUG_MODE_TAG);
            onNotificationTapIntent = Intent.getIntent(shared.getString(getResources().getString(R.string.pref_key_tapOnIntent), ""));
            pushFlag = shared.getBoolean(getResources().getString(R.string.push_notification_flag), false);


            GaePropertyId = shared.getString(getResources().getString(R.string.pref_key_gae_id), null);
            categoryNormalEvent = shared.getString(getResources().getString(R.string.pref_key_gae_categ_normal), null);
            categoryCrashEvent = shared.getString(getResources().getString(R.string.pref_key_gae_categ_crash), null);

            notificationTitle = shared.getString(getResources().getString(R.string.pref_key_notification_title), getString(R.string.natificationRateTitle));
            notificationSubtitle = shared.getString(getResources().getString(R.string.pref_key_notification_subtitle), getString(R.string.natificationRateTitle));
            notificationIcon = shared.getInt(getResources().getString(R.string.pref_key_notification_icon), R.drawable.ic_launcher);

            notificationImageTitle = shared.getString(getResources().getString(R.string.pref_key_notification_image_title), getString(R.string.natificationRateTitle));
            notificationImageSubtitle = shared.getString(getResources().getString(R.string.pref_key_notification_image_subtitle), getString(R.string.natificationRateTitle));
            notificationSmallIcon = shared.getInt(getResources().getString(R.string.pref_key_notification_image_icon_small), R.drawable.ic_launcher);
            notificationBigIcon = shared.getInt(getResources().getString(R.string.pref_key_notification_image_icon_big), R.drawable.ic_launcher);
            notificationLayout = shared.getInt(getResources().getString(R.string.pref_key_notification_image_layout), R.layout.support_simple_spinner_dropdown_item);

            isnotificationImage = shared.getBoolean(getResources().getString(R.string.pref_key_notification_image_isimage), false);

            valueIntent =  shared.getString(getResources().getString(R.string.pref_key_notification_image_value_intent), getResources().getString(R.string.pref_key_notification_image_value_intent_default));


        } catch (Exception ignored) { }
    }
}
