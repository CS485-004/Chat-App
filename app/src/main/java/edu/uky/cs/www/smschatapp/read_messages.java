package edu.uky.cs.www.smschatapp;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class read_messages extends ActionBarActivity {

    private TextView displayName;
    private Button composeButton;
    private ListView textField;
    ArrayList<String> smsMessagesList = new ArrayList<String>();

    ListView smsListView;       //The ListView for this activity
    ArrayAdapter arrayAdapter;  //An array adapter to put sms messages into the ListView

    private final String TAG = "SMSCHATAPP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_messages);

        composeButton = (Button) findViewById(R.id.composeButton);
        displayName = (TextView) findViewById(R.id.contactName);

        //You can see read_messages in the res layout folder: activity_read_messages.xml
        setContentView(R.layout.activity_read_messages);
        //list_sms_contact is the main listview in read_messages.xml
        smsListView = (ListView) findViewById(R.id.list_sms_contact);
        //Bind the info to our listview
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        smsListView.setAdapter(arrayAdapter);
        refreshSmsInbox();

    }

    public void refreshSmsInbox() {

        // Getting the contact number from the intent
        String contactNumber="";
        Intent intent = getIntent();
        contactNumber = intent.getStringExtra(TAG);

        String[] qStr = new String[]{contactNumber};

        // Getting all of the inbox messages
        ContentResolver inboxResolver = getContentResolver();
        Cursor smsInboxCursor = getContentResolver().query(Uri.parse("content://sms/inbox"), new String[] { "_id", "thread_id", "address", "person", "date","body", "type" }, "address=?", qStr, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        int indexDate = smsInboxCursor.getColumnIndex("date");
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();

        // Get this contact's name
        String contactName = getContactDisplayNameByNumber(contactNumber);
        displayName.setText(contactName);

        // Getting all of the sent sms messages
        ContentResolver sentResolver = getContentResolver();

        Cursor smsSentCursor = getContentResolver().query(Uri.parse("content://sms/sent"), new String[] { "_id", "thread_id", "address", "person", "date","body", "type" }, "address=?", qStr, null);;

        boolean sentSMSExist = true;
        long longSentDate = Long.MAX_VALUE;
        String sentNumber, strSentDate, formattedSentDate;
        if (!smsSentCursor.moveToFirst()){
            sentSMSExist = false;
        } else {
            sentNumber = smsSentCursor.getString(indexAddress);
            strSentDate = smsSentCursor.getString(indexDate);
            longSentDate = Long.parseLong(strSentDate, 10);
        }

        long longGotDate;
        String strGotNumber, strGotDate, formattedGotDate;
        do {
            //Start processing the sms messages gotten from the other number.
            // Get the next number in the inbox
            strGotNumber = smsInboxCursor.getString(indexAddress);
            // Get the date sent
            strGotDate = smsInboxCursor.getString(indexDate);
            longGotDate = Long.parseLong(strGotDate, 10);
            // Format that date
            formattedGotDate = new SimpleDateFormat("MM/dd/yyyy").format(longSentDate);

            // The message that will be visible
            String str = contactName + " sent at " + formattedGotDate + ":" +
                    "\n" + smsInboxCursor.getString(indexBody) + "\n";

            Log.v(TAG, "GOT: " + strGotNumber);
            // If this number is from the contact, add it to the list.
            if(longGotDate >= longSentDate){
                arrayAdapter.add(str);
                Log.v(TAG, "ADDED: " + str);
            }
            else {
                boolean advance = false;
                do {
                    // Do the same thing to get the next sent message
                    sentNumber = smsSentCursor.getString(indexAddress);
                    strSentDate = smsSentCursor.getString(indexDate);
                    longSentDate = Long.parseLong(strSentDate, 10);
                    formattedSentDate = new SimpleDateFormat("MM/dd/yyy").format(longSentDate);
                    Log.v(TAG, "SENT: " + sentNumber);

                    String sentStr = "I sent at " + formattedSentDate + ":\n"
                            + smsSentCursor.getString(indexBody) + "\n";
                    advance = false;
                    if (longGotDate < longSentDate){
                        arrayAdapter.add(sentStr);
                        Log.v(TAG, "ADDED: " + sentStr);
                        advance = true;
                    }
                } while (advance && smsSentCursor.moveToNext() && longGotDate < longSentDate);

                // Now add that last message that wasn't added before because longGotDate <= longSentDate
                arrayAdapter.add(str);
                Log.v(TAG, "ADDED: " + str);
            }

        } while (smsInboxCursor.moveToNext());
    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_read_messages, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void composeMessage(View view) {

        Intent intent = new Intent(this, ComposeSMS.class);
        startActivity(intent);
    }
}
