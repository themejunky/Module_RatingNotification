package com.example.tj_notifyrating.recivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.tj_notifyrating.R;
import com.example.tj_notifyrating.utils.Stuff;

import java.util.ArrayList;

import module.themejunky.com.tj_gae.Module_GoogleAnalyticsEvents;

import static com.example.tj_notifyrating.utils.Constants.DEFAULT_DEBUG_MODE;
import static com.example.tj_notifyrating.utils.Constants.DEFAULT_DEBUG_MODE_TAG;

public class ShutdownReceiver extends BroadcastReceiver {

    private Module_GoogleAnalyticsEvents mGae;

    @Override
    public void onReceive(Context context, Intent intent) {
        ArrayList<String> logsCollector = new ArrayList<>();
        logsCollector.add("*");
        logsCollector.add("------- ShutdownReceiver");

        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean pushFlag=shared.getBoolean(context.getResources().getString(R.string.push_notification_flag), false);
        Boolean pushIdle=shared.getBoolean(context.getResources().getString(R.string.push_notification_idle), false);
        Boolean debugMode = shared.getBoolean(context.getResources().getString(R.string.pref_key_debug), DEFAULT_DEBUG_MODE);
        int nrTimesNotficationAppear = shared.getInt(context.getString(R.string.pref_key_timeNotificationAppears), 0);
        String debugModeTag = shared.getString(context.getResources().getString(R.string.pref_key_debug_tag), DEFAULT_DEBUG_MODE_TAG);
        String gaePropertyId = shared.getString(context.getString(R.string.pref_key_gae_id), null);
        String categoryNormalEvent = shared.getString(context.getResources().getString(R.string.pref_key_gae_categ_normal), null);

        Stuff myStuff = new Stuff();

        logsCollector.add("*");
        logsCollector.add("* PushFlag : " + pushFlag);
        logsCollector.add("* PushIdle : " + pushIdle);
        logsCollector.add("* GaePropertyId : "+ gaePropertyId);

        if (gaePropertyId != null) {
            mGae = Module_GoogleAnalyticsEvents.getInstance(context, gaePropertyId);
            if (debugMode) {
                mGae.setDebug(debugModeTag);
            }
        }

        if (mGae!=null) { mGae.getEvents(categoryNormalEvent,"NOTIFY_RATING_LIB (ShutDown_Reciver)","Nothing (" + nrTimesNotficationAppear+")"); }

        myStuff.showLogs(debugMode,debugModeTag,logsCollector);
    }
}
