package edu.uky.cs.www.smschatapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.Calendar;


public class SchedulerActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduler);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scheduler, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_compose) {
            Intent intent = new Intent(this, ComposeSMS.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void createAlarm(View button) {
        // create the alarm
        final EditText editPhoneNumber = (EditText) findViewById(R.id.alarm_number);
        String toPhoneNumber = editPhoneNumber.getText().toString();

        final EditText editMessage = (EditText) findViewById(R.id.alarm_message);
        String message = editMessage.getText().toString();

        final EditText editAlarmDelay = (EditText) findViewById(R.id.alarm_delay);
        String alarmDelay = editAlarmDelay.getText().toString();

        Calendar calendar = Calendar.getInstance();
        // Start 30 seconds after boot completed
        calendar.add(Calendar.SECOND, Integer.valueOf(alarmDelay));
        Intent intent = new Intent(this, SchedulerService.class);

        intent.putExtra(String.valueOf(R.id.alarm_number), toPhoneNumber);
        intent.putExtra(String.valueOf(R.id.alarm_message), message);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pintent);

        Intent destinationIntent = new Intent(this, MainActivity.class);
        startActivity(destinationIntent);
    }
}
