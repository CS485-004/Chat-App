package edu.uky.cs.www.smschatapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;

import java.util.Calendar;

public class SettingsActivity extends ActionBarActivity {

    // Keys as defined in settings.xml
    public static String NOTIFICATIONS_KEY = "pref_notifications";
    //public static String VIBRATION_KEY = "pref_vibration";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Create a default empty ActionBar
        ActionBar actionBar = getSupportActionBar();

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    /**
     * SettingsFragment
     * Builds a Preferences screen based on the content in settings.xml
     */
    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings);
        }
    }
}