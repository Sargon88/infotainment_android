package esardini.com.infotainment2.service;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import esardini.com.infotainment2.bean.CallHistoryBean;
import esardini.com.infotainment2.bean.ContactBean;

public class CallPageService {

    private ArrayList<ContactBean> starredContacts;
    private ArrayList<CallHistoryBean> lastCalls;

    private static final String TAG = "CallPageService";

    public ArrayList<ContactBean> getStarredContacts(Context c) {
        buildStarredContactsList(c);

        return starredContacts;
    }

    public List<CallHistoryBean> getLastCalls(Context c) {
        buildLastCallsList(c);

        return lastCalls.subList(0, 5);
    }

    public String getStarredContactsJSON(Context c) {
        ArrayList<ContactBean> contact = getStarredContacts(c);
        Gson g = new Gson();
        String json = g.toJson(contact);

        return json;
    }

    public String getLastCallsJSON(Context c) {
        List<CallHistoryBean> tempLastCalls = getLastCalls(c);

        Gson g = new Gson();
        String lastCallsJson = g.toJson(lastCalls.subList(0,5));

        return lastCallsJson;
    }

    private void buildStarredContactsList(Context context) {

        ArrayList<ContactBean> contactMap = new ArrayList<ContactBean>();

        Uri queryUri = ContactsContract.Contacts.CONTENT_URI;

        String[] projection = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.STARRED};

        String selection = ContactsContract.Contacts.STARRED + "='1'";

        Cursor cursor = context.getContentResolver().query(queryUri, projection, selection, null, null);

        while (cursor.moveToNext()) {
            ContactBean cb = new ContactBean();

            String contactId = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts._ID));

            String title = (cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));

            cb.setName(title);

            Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
            while (phones.moveToNext()) {
                String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Log.v(TAG, "number: " + number);

                int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

                Log.v(TAG, "type: " + type);

                switch (type) {
                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                        // do something with the Home number here...
                        break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                        cb.setNumber(number);
                        break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                        // do something with the Work number here...
                        break;
                }
            }

            Log.v(TAG, "cb number: " + cb.getNumber());

            contactMap.add(cb);
        }


        cursor.close();
        starredContacts = contactMap;

    }

    private void buildLastCallsList(Context context) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ArrayList<CallHistoryBean> tempLastCalls = new ArrayList<CallHistoryBean>();

        Cursor managedCursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int number = managedCursor.getColumnIndex( CallLog.Calls.NUMBER );
        int type = managedCursor.getColumnIndex( CallLog.Calls.TYPE );
        int date = managedCursor.getColumnIndex( CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex( CallLog.Calls.DURATION);


        while ( managedCursor.moveToNext() ) {
            String phNumber = managedCursor.getString( number );
            String callType = managedCursor.getString( type );
            String callDate = managedCursor.getString( date );
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = managedCursor.getString( duration );
            String dir = null;
            int dircode = Integer.parseInt( callType );
            switch( dircode ) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }

            //estrarre il nome del contatto
            String contactName = getContactName(phNumber, context);

            //TODO rivedere il name
            CallHistoryBean c = new CallHistoryBean(phNumber, callType, dir, callDate, callDayTime, callDuration, contactName);

            tempLastCalls.add(c);

        }

        managedCursor.close();
        lastCalls = tempLastCalls;
    }

    private String getContactName(String phoneNumber, Context context){
        Uri uri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName=null;
        Cursor cursor = context.getContentResolver().query(uri,projection,null,null,null);

        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contactName=cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }
}
