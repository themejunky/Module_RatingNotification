package com.example.tj_notifyrating;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.preference.PreferenceManager;

import com.example.tj_notifyrating.utils.Stuff;

import java.util.ArrayList;
import java.util.List;

import module.themejunky.com.tj_gae.Module_GoogleAnalyticsEvents;

import static com.example.tj_notifyrating.utils.Constants.DEFAULT_DEBUG_MODE;
import static com.example.tj_notifyrating.utils.Constants.DEFAULT_DEBUG_MODE_TAG;
import static com.example.tj_notifyrating.utils.Constants.DEFAULT_DELAY_BETWEEN_ATTEMPTS_INTERNET_NOT_SURE;
import static com.example.tj_notifyrating.utils.Constants.DEFAULT_DELAY_BETWEEN_NOTIFICATIONS;
import static com.example.tj_notifyrating.utils.Constants.DEFAULT_NR_OF_NOTIFICATIONS;

public class Module_NotifyRating {
    private Module_GoogleAnalyticsEvents mGae;
    /* if it's set it contains Google Analytics Id for sending events */
    private String GaePropertyId;
    /* Main category Event for normal events */
    private String categoryNormalEvent;
    /* Main category Event for crash events */
    private String categoryCrashEvent;
    /* Name of the class that should be open when user tap on notification */
    private Class<?> className;
    /* milliseconds delay between notifications*/
    private int millisSecondsDelay = DEFAULT_DELAY_BETWEEN_NOTIFICATIONS;
    /* milliseconds delay between new attempts when internet connection is not secure*/
    private int millisSecondsAttempts = DEFAULT_DELAY_BETWEEN_ATTEMPTS_INTERNET_NOT_SURE;
    /* how many notifications to send */
    private int nrOfNotifications = DEFAULT_NR_OF_NOTIFICATIONS;
    /* if set to "true" Log.d will print */
    private Boolean debugMode = DEFAULT_DEBUG_MODE;
    /* debug mode TAG for Log.d */
    private String debugModeTag = DEFAULT_DEBUG_MODE_TAG;
    /* The intent for tap on notification action */
    private Intent onNotificationTapIntent;
    /* Contains the prefered flags set by user for the onNotificationTapIntent; If this is null ...default flags will be set */
    private ArrayList<Integer> custonIntentFlags = null;
    /* Needed for context/activity */
    private Activity activity;
    /* ArrayList that will colect all logs and print at the end if "debugMode" is true */
    private ArrayList<String> logsCollector;
    /* Name of the Service */
    private Class<?> serviceName = ServiceNotification.class;
    /* If set, libray will search for all apps that contains given pakageName and if more than 1 are find : the Service will never run on this instance*/
    private String packageName = "";
    /* Stuff colection */
    private Stuff myStuff;
    /* SharePref */


    private SharedPreferences shared;
    private SharedPreferences.Editor sharedEdit;

    /**
     * CONSTRUCTOR_CLASS
     *
     * @param activity       - activity
     * @param disableService - dislable service
     */
    @SuppressLint("CommitPrefEdits")
    public Module_NotifyRating(Activity activity, Boolean disableService) {
        this.activity = activity;
        this.myStuff = new Stuff();
        shared = PreferenceManager.getDefaultSharedPreferences(activity);
        sharedEdit = PreferenceManager.getDefaultSharedPreferences(activity).edit();
        if (!disableService) {
            disableNotification();
        }
    }

    /**
     * SETTER METHOD
     *
     * @param activity    - activity
     * @param className   - activity name that should be open when user tap on notification
     * @param packageName - common name of the packageName to search for any other instance flavors
     */
    @SuppressWarnings("unused")
    public Module_NotifyRating(Activity activity, Class<?> className, String packageName) {
        this.activity = activity;
        this.className = className;
        this.myStuff = new Stuff();
        if (packageName != null) {
            this.packageName = packageName;
        }

        shared = PreferenceManager.getDefaultSharedPreferences(activity);
        sharedEdit = PreferenceManager.getDefaultSharedPreferences(activity).edit();
    }


    /**
     * SETTER METHOD
     *
     * @param title    - custom title for notification
     * @param subtitle - custom subtitle for notification
     * @param icon     - custom icon drawable for notification
     * @return current instance
     */
    @SuppressWarnings("unused")
    public Module_NotifyRating set_TextAndIcon(String title, String subtitle, int icon) {
        sharedEdit.putString(activity.getResources().getString(R.string.pref_key_notification_title), title);
        sharedEdit.putString(activity.getResources().getString(R.string.pref_key_notification_subtitle), subtitle);
        sharedEdit.putInt(activity.getResources().getString(R.string.pref_key_notification_icon), icon);
        sharedEdit.apply();
        return this;
    }


    /**
     * SETTER METHOD
     *
     * @param title    - custom title for notification
     * @param subtitle - custom subtitle for notification
     * @param smallIcon     - custom icon drawable for notification
     * @param bigIcon    - custom icon drawable for notification
     * @param layout     - custom layout for big image notification
     * @return current instance
     */

    public Module_NotifyRating set_TextAndIconAndImage(String title, String subtitle, int smallIcon,int bigIcon,int layout) {
        sharedEdit.putString(activity.getResources().getString(R.string.pref_key_notification_image_title), title);
        sharedEdit.putString(activity.getResources().getString(R.string.pref_key_notification_image_subtitle), subtitle);
        sharedEdit.putInt(activity.getResources().getString(R.string.pref_key_notification_image_icon_small), smallIcon);
        sharedEdit.putInt(activity.getResources().getString(R.string.pref_key_notification_image_icon_big), bigIcon);
        sharedEdit.putInt(activity.getResources().getString(R.string.pref_key_notification_image_layout), layout);
        sharedEdit.apply();
        return this;
    }

    /**
     * SETTER METHOD
     *
     * @param millisSecondsDelay    - delay until notification should appear
     * @param nrOfNotifications     - number of times notification should be send ( hoursDelay between them )
     * @param millisSecondsAttempts - millis delay between attempts when internet is not secure
     */
    @SuppressWarnings("unused")
    public Module_NotifyRating set_HoursAndRepeateTimes(int millisSecondsDelay, int nrOfNotifications, int millisSecondsAttempts) {
        this.millisSecondsDelay = millisSecondsDelay;
        this.nrOfNotifications = nrOfNotifications;
        this.millisSecondsAttempts = millisSecondsAttempts;
        return this;
    }

    /**
     * SETTER METHOD
     *
     * @param newIntentFlag - arrayList of new flags to be set for the intent that opens application when tapping notification
     */
    @SuppressWarnings("unused")
    public Module_NotifyRating set_NewFlagsForOnTapIntent(ArrayList<Integer> newIntentFlag) {
        this.custonIntentFlags = newIntentFlag;
        return this;
    }

    @SuppressWarnings("unused")
    public Module_NotifyRating set_GaePropertyId(String GaePropertyId, String categoryNormalEvent, String categoryCrashEvent) {
        this.GaePropertyId = GaePropertyId;
        this.categoryNormalEvent = categoryNormalEvent;
        this.categoryCrashEvent = categoryCrashEvent;
        return this;
    }

    /**
     * SETTER METHOD
     *
     * @param debugTag - debugTagName
     */
    @SuppressWarnings("unused")
    public Module_NotifyRating set_DebugMode(String debugTag) {
        this.debugMode = true;
        this.debugModeTag = debugTag;
        return this;
    }

    /**
     * By this method all starts
     */
    @SuppressLint("CommitPrefEdits")
    public void start() {
        logsCollector = new ArrayList<>();
        /* creating GAE instance if propertyId is provided */
        try {
            if (GaePropertyId != null) {
                mGae = Module_GoogleAnalyticsEvents.getInstance(activity, GaePropertyId);
                if (debugMode) {
                    mGae.setDebug(debugModeTag);
                }
            }
        } catch (Exception e) {
            myStuff.showErrorLog(debugMode, debugModeTag, "* NOTIFY_RATING_LIB : (start GAE) : "+e.getMessage());
        }



        try {

            logsCollector.add("**********************Start Module Rating**********************");

            if (setUp_searchForFlavors()) {
                logsCollector.add("* MORE THAN ONE FLAVORS ; NO SERVICE ; CLOSE LIB");
                if (mGae!=null) { mGae.getEvents(categoryNormalEvent,"NOTIFY_RATING_LIB (no-start)","more than 1 flavors"); }
                myStuff.showLogs(debugMode, debugModeTag, logsCollector);
                return;
            }

            logsCollector.add("*");
            logsCollector.add("* Service Status [ON(true)/OFF(false)] : " + stateOfNotification());

            if (!wasServiceStarted()) {
                logsCollector.add("* Service Info : was not previously initialized ");
                logsCollector.add("*");
                logsCollector.add("* SetUp :");

                try {
                    setUp_IntentOfNotification();
                } catch (Exception e) {
                    myStuff.showErrorLog(debugMode, debugModeTag, "* NOTIFY_RATING_LIB (start-1) : "+e.getMessage());
                    if (mGae!=null) { mGae.getEvents(categoryCrashEvent,"NOTIFY_RATING_LIB (start-1)","Error : "+e.getMessage()); }
                }


                try {
                   setUp_StartService();
                } catch (Exception e) {
                    myStuff.showErrorLog(debugMode, debugModeTag, "* NOTIFY_RATING_LIB (start-2) : "+e.getMessage());
                    if (mGae!=null) { mGae.getEvents(categoryCrashEvent,"NOTIFY_RATING_LIB (start-2)","Error : "+e.getMessage()); }
                }


                try {
                    setUp_SaveSettingsInPref();
                } catch (Exception e) {
                    myStuff.showErrorLog(debugMode, debugModeTag, "* NOTIFY_RATING_LIB (start-3) : "+e.getMessage());
                    if (mGae!=null) { mGae.getEvents(categoryCrashEvent,"NOTIFY_RATING_LIB (start-3)","Error : "+e.getMessage()); }
                }


                try {
                    setUp_ShowVariables();
                } catch (Exception e) {
                    myStuff.showErrorLog(debugMode, debugModeTag, "* NOTIFY_RATING_LIB (start-4) : "+e.getMessage());
                    if (mGae!=null) { mGae.getEvents(categoryCrashEvent,"NOTIFY_RATING_LIB (start-4)","Error : "+e.getMessage()); }
                }

            } else {
                if (mGae!=null) { mGae.getEvents(categoryNormalEvent,"Start","Library was prev started"); }
                logsCollector.add("* Service Info :  has been previously initialized");
                logsCollector.add("*");
            }
            myStuff.showLogs(debugMode, debugModeTag, logsCollector);
            if (mGae!=null) { mGae.getEvents(categoryNormalEvent,"NOTIFY_RATING_LIB","Library started"); }

        } catch (Exception e) {
            myStuff.showErrorLog(debugMode, debugModeTag, "* NOTIFY_RATING_LIB (start-0) : "+e.getMessage());
            if (mGae!=null) { mGae.getEvents(categoryCrashEvent,"NOTIFY_RATING_LIB (start-0)","Error : "+e.getMessage()); }
        }
    }

    /**
     * In this method a search for any flavor is made using packageName provided ( if it was provided )
     * <p>
     * return false : if no packageName was provided or if just one flavor is found
     *
     * @return true : if more than 1 flavor is found ( assume that the first installed flavor has a running instance of this library
     */
    private boolean setUp_searchForFlavors() {

        if (packageName.length() < 3) {
            logsCollector.add("*");
            logsCollector.add("* No packageName provided ; Service will run : true");
            return false;
        }

        int flavorsCount = 0;
        PackageManager manager = activity.getPackageManager();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
        for (ResolveInfo ri : availableActivities) {
            if (ri.activityInfo.packageName.contains(packageName)) {
                flavorsCount++;
            }
        }

        if (flavorsCount < 2) {
            logsCollector.add("*");
            logsCollector.add("* Less than 2 flavors for entered packageNme(" + packageName + "); Service will run : true");
            return false;
        } else {
            logsCollector.add("*");
            logsCollector.add("* Mote than 1 flavors for entered packageNme(" + packageName + ") ; Service will run : false");
            return true;
        }
    }

    private void setUp_ShowVariables() {
        logsCollector.add("*");
        logsCollector.add("* Variables : ");
        logsCollector.add("* Delay Between any 2 notifications : " + millisSecondsDelay + " (millis) | "+myStuff.millisToTime(millisSecondsDelay));
        logsCollector.add("* Delay Between attempts internetNoSure : " + millisSecondsAttempts + " (millis) | "+myStuff.millisToTime(millisSecondsAttempts));
        logsCollector.add("* Number of notification to be set : " + nrOfNotifications);
        logsCollector.add("* Notification Title : " + shared.getString(activity.getResources().getString(R.string.pref_key_notification_title), activity.getResources().getString(R.string.natificationRateTitle)));
        logsCollector.add("* Notification Subtitle : " + shared.getString(activity.getResources().getString(R.string.pref_key_notification_subtitle), activity.getResources().getString(R.string.natificationRateMessage)));
        logsCollector.add("* GAE : " + shared.getString(activity.getResources().getString(R.string.pref_key_gae_id),null));
        logsCollector.add("* GAE_categ_normal : " + shared.getString(activity.getResources().getString(R.string.pref_key_gae_categ_normal),null));
        logsCollector.add("* GAE_categ_crash : " + shared.getString(activity.getResources().getString(R.string.pref_key_gae_categ_crash),null));
        logsCollector.add("*");
    }

    private void setUp_SaveSettingsInPref() {
        sharedEdit.putString(activity.getResources().getString(R.string.pref_key_tapOnIntent), onNotificationTapIntent.toURI());
        sharedEdit.putString(activity.getResources().getString(R.string.pref_key_gae_id), GaePropertyId);
        sharedEdit.putString(activity.getResources().getString(R.string.pref_key_gae_categ_normal), categoryNormalEvent);
        sharedEdit.putString(activity.getResources().getString(R.string.pref_key_gae_categ_crash), categoryCrashEvent);

        sharedEdit.putInt(activity.getResources().getString(R.string.pref_key_millisSecondsDelayNotification), millisSecondsDelay);
        sharedEdit.putInt(activity.getResources().getString(R.string.pref_key_millisSecondsDelayAttempts), millisSecondsAttempts);
        sharedEdit.putInt(activity.getResources().getString(R.string.pref_key_nrTimes), nrOfNotifications);
        sharedEdit.putBoolean(activity.getResources().getString(R.string.pref_key_debug), debugMode);
        sharedEdit.putString(activity.getResources().getString(R.string.pref_key_debug_tag), debugModeTag);
        sharedEdit.putBoolean(activity.getResources().getString(R.string.push_notification_was_service_started), true);
        sharedEdit.putBoolean(activity.getResources().getString(R.string.push_notification_flag), true);
        sharedEdit.apply();

        logsCollector.add("* 3) Save Settings in Pref");
    }

    /**
     * Prepare and set the intent for on tap notification
     */
    private void setUp_IntentOfNotification() {

        if (custonIntentFlags == null) {
            custonIntentFlags = new ArrayList<>();
            custonIntentFlags.add(Intent.FLAG_ACTIVITY_NEW_TASK);
            custonIntentFlags.add(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            custonIntentFlags.add(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            custonIntentFlags.add(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            logsCollector.add("* 1) Set onTapNotificationIntent flags : Default (" + custonIntentFlags.size() + " flags)");
        } else {
            logsCollector.add("* 1) Set onTapNotificationIntent flags : Custom (" + custonIntentFlags.size() + " flags)");
        }

        onNotificationTapIntent = new Intent();
        onNotificationTapIntent.setClass(activity, className);
        for (Integer flag : custonIntentFlags) {
            onNotificationTapIntent.setFlags(flag);
        }
        onNotificationTapIntent.putExtra(activity.getResources().getString(R.string.intent_key_notification), "click");
    }

    /**
     * Prepare and start the service that will sleep and wake up to send notification
     */
    private void setUp_StartService() {
        Intent i = new Intent(activity, serviceName);
        i.putExtra(activity.getResources().getString(R.string.intent_key_intent), onNotificationTapIntent);
        i.putExtra(activity.getResources().getString(R.string.intent_key_first_launch), "yes");
        activity.startService(i);

        logsCollector.add("* 2) Start Service");
    }

    /**
     * Shut down ALL
     */
    private void disableNotification() {
        sharedEdit.putBoolean(activity.getResources().getString(R.string.push_notification_flag), false);
        sharedEdit.putBoolean(activity.getResources().getString(R.string.push_notification_idle), false);
        sharedEdit.apply();
    }

    /**
     * Check if service was started to prevet any new starts
     *
     * @return ture if was started
     */
    private boolean wasServiceStarted() {
        return shared.getBoolean(activity.getResources().getString(R.string.push_notification_was_service_started), false);
    }

    /**
     * Check if Service is ON(false)/OFF(true)
     *
     * @return ture if was started
     */
    private boolean stateOfNotification() {
        return shared.getBoolean(activity.getResources().getString(R.string.push_notification_flag), true);
    }
}
