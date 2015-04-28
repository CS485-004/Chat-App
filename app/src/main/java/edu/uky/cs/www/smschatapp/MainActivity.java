package edu.uky.cs.www.smschatapp;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
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

    private final String TAG = "SMSCHATAPP";

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

        //Set default preference values
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

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
        int i = 0;
        do {
            String phoneNumber = smsInboxCursor.getString(indexAddress);
            String contactName = getContactDisplayNameByNumber(phoneNumber);

            String str = phoneNumber + " : " + contactName +
                    "\n" + smsInboxCursor.getString(indexBody) + "\n";
            arrayAdapter.add(str);
        } while (smsInboxCursor.moveToNext() && (i = i +1) < 100);
    }

    /**
     * getContactDisplayNameByNumber
     * Purpose:
     *  Gets the contact name if exising from the contacts.
     * Preconditions:
     * @param number A string representing a phone number
     * @return
     * Post-conditions:
     *  Returns either a contact name or "?"
     */
    public String getContactDisplayNameByNumber(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = "?";

        ContentResolver contentResolver = getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        return name;
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
            Intent intent = new Intent(this, read_messages.class);

            //Getting the phone number
            String phoneNumber = address.split("\\s+")[0];
            intent.putExtra(TAG, phoneNumber);
            Log.v(TAG, smsMessage);
            // Finally, launch the activity.
            startActivity(intent);


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
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
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
        if (id == R.id.action_alarm) {
            Intent intent = new Intent(this, SchedulerActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
