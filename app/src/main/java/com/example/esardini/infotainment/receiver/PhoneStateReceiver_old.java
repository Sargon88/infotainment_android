package com.example.esardini.infotainment.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
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

public class PhoneStateReceiver_old extends BroadcastReceiver {

    private static String TAG = "PhoneStateReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
         Log.i("ccc " + TAG, "ENTERED onReceive");

        String action = intent.getAction();

        if(Constants.telephony == null) {
             Constants.telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
             //Constants.listener = new MyPhoneStateListener();
             //Constants.telephony.listen(Constants.listener, PhoneStateListener.LISTEN_CALL_STATE);
         }
    }

    public class MyPhoneStateListener extends PhoneStateListener {
        private final String TAG = "MyPhoneStateListener";
        private int prevState;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Log.i("ccc "+TAG, "onCallStateChanged - state: " + state);

            String callState = "UNKNOWN";

            if(MainActivity.tw == null) {
                MainActivity.tw = ((Activity) Constants.context).findViewById(R.id.number);
            }


            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    callState = "IDLE";

                    MainActivity.tw.setText("number");

                    MainActivity.sendDataToRaspberry("call end", incomingNumber);

                    MainActivity.collectStatusData();

                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    // -- check international call or not.
                    callState = "CALL_STATE_RINGING";

                    MainActivity.tw.setText("Caller: " + incomingNumber);

                    String json = getExtraCallData(incomingNumber, "in");

                    //String msg = "{\"number\": \""+ incomingNumber +"\", \"name\": \""+ name +"\", \"date\": \""+ date +"\", \"photo\": \""+ base64Image +"\"}";

                    MainActivity.sendDataToRaspberry("incoming calling", json);

                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    callState = "CALL_STATE_OFFHOOK";

                    String action = "";
                    String msg = incomingNumber;

                    if(prevState == TelephonyManager.CALL_STATE_RINGING){
                        //incoming
                        action = "call answer";
                    }

                    if(prevState == TelephonyManager.CALL_STATE_IDLE){
                        //outgoing
                        action = "outgoing calling";

                        String number = incomingNumber;
                        msg = getExtraCallData(number, "out");
                    }

                    MainActivity.sendDataToRaspberry(action, msg);

                    break;
            }

            prevState = state;
            Log.i("bbb "+">>>Broadcast", "onCallStateChanged " + callState);
            super.onCallStateChanged(state, incomingNumber);
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            MainActivity.mSignalStrength = signalStrength.getGsmSignalStrength();
            MainActivity.mSignalStrength = (2 * MainActivity.mSignalStrength) - 113; // -> dBm
        }
    }

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
