package com.themejunky.module.notifyrating;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.tj_notifyrating.Module_NotifyRating;

public class Main2Activity extends AppCompatActivity {
    public Module_NotifyRating notifyRating;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        notifyRating = new Module_NotifyRating(this,true,Main2Activity.class,true);
        notifyRating.closePush();
    }
}
