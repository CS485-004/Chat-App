package edu.uky.cs.www.smschatapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * MainActivity.java
 *
 * Purpose:
 *  Currently shows all SMS messages in the sms inbox.
 *  Extends ActionBarActivity (for settings and other options)
 *  Implements AdapterView.OnItemClickListener  so the user can click on messages.
 */

/**
 * These tutorials were helpful:
 * http://javapapers.com/category/android/
 */

//The ActionBarActivity is the top menu bar in Activities
public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private static MainActivity inst;
    ArrayList<String> smsMessagesList = new ArrayList<String>();

    ListView smsListView;       //The ListView for this activity
    ArrayAdapter arrayAdapter;  //An array adapter to put sms messages into the ListView

    //Singleton pattern
    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    /**
     * onCreate
     * Purpose:
     *  This function is called when the activity is first created.
     * Preconditions:
     *  @param savedInstanceState
     * Postconditions:
     *  Populates and displays the main ListView
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Start the service to display notifications
        Intent notificationServiceIntent = new Intent(this, NotificationService.class);
        startService(notificationServiceIntent);

        //You can see activity_main in the res folder: activity_main.xml
        setContentView(R.layout.activity_main);
        //SMSList is the main listview in activity_main.xml
        smsListView = (ListView) findViewById(R.id.SMSList);
        //Bind the info to our listview
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        smsListView.setAdapter(arrayAdapter);
        smsListView.setOnItemClickListener(this);

        //test
        //Build the notification:
        /*NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_action_chat)
                .setContentTitle("Message from test")
                .setContentText("test");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        // The stack builder object will contain an artificial back stack for the started Activity.
        // This ensures that navigating backward from the Activity leads out of your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int id = 123; //needs to be an integer unique to this application to be able to update the notification later on after it's created. (or could create multiple notifications?)
        notificationManager.notify(id, builder.build());*/

        //Helper function
        refreshSmsInbox();
    }

    /**
     * refreshSMSInbox
     * Purpose:
     *  This is the helper function that actually populates the list.
     */
    public void refreshSmsInbox() {
        ContentResolver contentResolver = getContentResolver();

        //Get the SMS inbox messages.  This is the query format for the SMS database
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        //"content://sms/sent" are the sent sms messages
        //"content://sms/draft" are the draft sms messages
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();
        //Add all these messages into the List
        do {
            String str = "SMS From: " + smsInboxCursor.getString(indexAddress) +
                    "\n" + smsInboxCursor.getString(indexBody) + "\n";
            arrayAdapter.add(str);
        } while (smsInboxCursor.moveToNext());
    }

    /**
     * Not currently used... can insert a message
     * @param smsMessage
     */
    public void updateList(final String smsMessage) {
        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();
    }

    /**
     * onItemClick
     * Purpose:
     *  When the user clicks on one of the list items.
     * Preconditions: these are standard:
     * @param parent
     * @param view
     * @param pos
     * @param id
     */
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        try {
            String[] smsMessages = smsMessagesList.get(pos).split("\n");
            //Include the address (phone number).
            String address = smsMessages[0];
            String smsMessage = "";
            //Get the correct message.
            for (int i = 1; i < smsMessages.length; ++i) {
                smsMessage += smsMessages[i];
            }
            //Creates the message string for displaying in the Toast
            String smsMessageStr = address + "\n";
            smsMessageStr += smsMessage;
            Toast.makeText(this, smsMessageStr, Toast.LENGTH_SHORT).show();

            /**
             * TODO:
             * This should really launch another activity for reading all messages
             * from that person.  Need to interleave sent messages to that person somehow as well.
             *
             * Intent intent = new Intent(this, AnotherActivity.class);
             * intent.putExtra(EXTRA_STRING, _id);
             * startActivity(intent);
             */

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * onCreateOptionsMenu
     * Purpose:
     *  This is the function that populates the action bar menu.
     * Preconditions:
     *  See the menu folder: menu_main.xml
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //This method handles user clicks on the menu option buttons.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //Currently does nothing.
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_compose) {
            /**
             * action_compose is defined in the activity_main.xml
             * Launches our Compose activity.
             */
            Intent intent = new Intent(this, ComposeSMS.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
