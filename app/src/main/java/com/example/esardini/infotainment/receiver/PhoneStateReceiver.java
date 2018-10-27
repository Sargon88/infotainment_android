package com.example.esardini.infotainment.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import com.example.esardini.infotainment.Constants;
import com.example.esardini.infotainment.MainActivity;
import com.example.esardini.infotainment.R;
import com.example.esardini.infotainment.bean.CallBean;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class PhoneStateReceiver extends BroadcastReceiver {

    private static String TAG = "PhoneStateReceiver";
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;

    @Override
    public void onReceive(Context context, Intent intent) {
         Log.i("ccc1 " + TAG, intent.getAction());

         if(intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")){
             savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");

         } else if(intent.getAction().equals("android.intent.action.PHONE_STATE")) {

             String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
             String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
             int state = 0;

             if(stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                 state = TelephonyManager.CALL_STATE_IDLE;
             } else if(stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                 state = TelephonyManager.CALL_STATE_OFFHOOK;
             } else if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                 state = TelephonyManager.CALL_STATE_RINGING;
             }


             onCallStateChanged(context, state, number);
         }

    }

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    public void onCallStateChanged(Context context, int state, String number) {
        Log.i("ccc " + TAG, "ENTERED onCallStateChanged");

        if(lastState == state){
            Log.i("ccc " + TAG, "No Change");
            //No change, debounce extras
            return;
        }

        if(MainActivity.tw == null) {
            MainActivity.tw = ((Activity) Constants.context).findViewById(R.id.number);
        }


        if(savedNumber != null && savedNumber != "") {
            MainActivity.tw.setText("Caller: " + savedNumber);
        } else {
            MainActivity.tw.setText("Caller: " + number);
        }

        Log.i("ccc " + TAG, "State: " + state);
        switch (state) {

            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                onIncomingCallStarted(context, number, callStartTime);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if(lastState != TelephonyManager.CALL_STATE_RINGING){
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, savedNumber, callStartTime);
                } else {

                    onIncomingCallAnswer(context, savedNumber, callStartTime);

                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:

                MainActivity.tw.setText("number");

                MainActivity.sendDataToRaspberry("call end", number);

                MainActivity.collectStatusData();

                //Useless at this time
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if(lastState == TelephonyManager.CALL_STATE_RINGING){
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber, callStartTime);
                }
                else if(isIncoming){
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                else{
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                break;
        }
        lastState = state;
    }


    //Derived classes should override these to respond to specific events of interest
    protected void onIncomingCallStarted(Context ctx, String number, Date start){
        Log.i("ccc " + TAG, "ENTERED onIncomingCallStarted");

        String json = getExtraCallData(number, "in");
        MainActivity.sendDataToRaspberry("incoming calling", json);
    }

    protected void onOutgoingCallStarted(Context ctx, String number, Date start){
        Log.i("ccc " + TAG, "ENTERED onIncomingCallStarted");

        String msg = getExtraCallData(number, "out");
        MainActivity.sendDataToRaspberry("outgoing calling", msg);
    }

    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end){
        Log.i("ccc " + TAG, "ENTERED onIncomingCallEnded");
    }

    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end){
        Log.i("ccc " + TAG, "ENTERED onOutgoingCallEnded");
    }

    protected void onMissedCall(Context ctx, String number, Date start){
        Log.i("ccc " + TAG, "ENTERED onMissedCall");
    }

    private void onIncomingCallAnswer(Context ctx, String number, Date start) {
        Log.i("ccc " + TAG, "ENTERED onIncomingCallAnswer");

        MainActivity.sendDataToRaspberry("call answer", number);

    }



    /** UTILITY */
    private String getExtraCallData(String incomingNumber, String type){

        Log.d("ccc " + TAG, "incoming NUmber: " + incomingNumber + " - type: " + type);

        //inizio
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));
        String name = "";
        String date = "";
        String base64Image = "";
        Long contactId;
        Bitmap photo = BitmapFactory.decodeResource(Constants.context.getResources(), R.drawable.default_user);

        ContentResolver contentResolver = Constants.context.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {
                BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                contactId = contactLookup.getLong(contactLookup.getColumnIndex(BaseColumns._ID));
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                //date = contactLookup.getString((contactLookup.getColumnIndex(ContactsContract.Data.LAST_TIME_CONTACTED)));

            }


        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }
        // fine

        CallBean cb = new CallBean(name, incomingNumber, date, type);
        Gson g = new Gson();
        String json = g.toJson(cb);

        Log.d("ccc " + TAG, json);

        return json;
    }

    private String getStringFromBitmap(Bitmap bitmapPicture) {
        /*
         * This functions converts Bitmap picture to a string which can be
         * JSONified.
         * */
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

        Log.i(TAG, "image: " + encodedImage);

        return encodedImage;
    }
}
