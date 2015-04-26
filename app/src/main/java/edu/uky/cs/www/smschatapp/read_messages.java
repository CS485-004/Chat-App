package edu.uky.cs.www.smschatapp;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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

        String contactNumber="";
        Intent intent = getIntent();
        contactNumber = intent.getStringExtra(TAG);

        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();

        // Get this contact's name
        String contactName = getContactDisplayNameByNumber(contactNumber);
        displayName.setText(contactName);

        do {
            // Get the next number in the inbox
            String thisNumber = smsInboxCursor.getString(indexAddress);

            String str = "SMS From: " + contactName +
                    "\n" + smsInboxCursor.getString(indexBody) + "\n";

            // If this number is from the contact, add it to the list.
            if(thisNumber.equals(contactNumber)){
                arrayAdapter.add(str);
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
