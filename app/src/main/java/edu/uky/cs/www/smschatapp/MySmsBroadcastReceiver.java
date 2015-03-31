package edu.uky.cs.www.smschatapp;
/**
 * Created by bryan March 19, 2015
 */
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsMessage;
import android.widget.Toast;


public class MySmsBroadcastReceiver extends BroadcastReceiver {
    public MySmsBroadcastReceiver() {
    }

    public static final String SMS_BUNDLE = "pdus";

    /**
     * onReceive
     *  Purpose:
     *      Listens for SMS messages and adds them to the main List.
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";
            String smsBody = "";
            String smsAddress = "";
            for (int i = 0; i < sms.length; ++i) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

                smsBody = smsMessage.getMessageBody();
                smsAddress = smsMessage.getOriginatingAddress();

                smsMessageStr += "SMS From: " + smsAddress + "\n";
                smsMessageStr += smsBody + "\n";
            }

            //Get the user's settings
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            //Get the value for notifications
            boolean notificationsOn = sharedPreferences.getBoolean(SettingsActivity.NOTIFICATIONS_KEY, true);

            if (notificationsOn) {
                //Build the notification:
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_action_chat)
                        .setContentTitle("Message from " + smsAddress)
                        .setContentText(smsBody);

                Intent notificationIntent = new Intent(context, MainActivity.class);
                // The stack builder object will contain an artificial back stack for the started Activity.
                // This ensures that navigating backward from the Activity leads out of your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                // Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(MainActivity.class);
                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(notificationIntent);
                PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                builder.setContentIntent(pendingIntent);
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                int id = 123; //TODO: needs to be an integer unique to this application to be able to update the notification later on after it's created. (or could create multiple notifications?)
                notificationManager.notify(id, builder.build());
            }

            Toast.makeText(context, smsMessageStr, Toast.LENGTH_SHORT).show();

            //This updates the UI with message
            MainActivity inst = MainActivity.instance();
            inst.updateList(smsMessageStr);
        }
    }
}

