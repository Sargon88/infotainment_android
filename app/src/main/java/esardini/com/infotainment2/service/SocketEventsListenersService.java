package esardini.com.infotainment2.service;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;
import java.net.URISyntaxException;

import esardini.com.infotainment2.constants.Params;
import esardini.com.infotainment2.constants.SocketEvents;
import esardini.com.infotainment2.constants.SocketSingleton;
import esardini.com.infotainment2.receiver.PhoneStateServiceRestarter;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketEventsListenersService extends Service{

    private static final String TAG = SocketEventsListenersService.class.getSimpleName() + " ccc";
    private UserStopServiceReceiver stopServiceRec;
    private static Context context;
    private Socket socket;

    private boolean restart = true;

    public SocketEventsListenersService(Context c){
        Log.i(TAG, "SocketEventsListenersService");
        context = c;
    }

    public SocketEventsListenersService(){}

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        startForeground(1, new Notification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "onStartCommand");

        if(stopServiceRec == null){
            stopServiceRec = new SocketEventsListenersService.UserStopServiceReceiver();
            registerReceiver(stopServiceRec, new IntentFilter(Params.KILL_SERVICE_REQUEST));
        }

        if(socket == null){
            try {
                socket =  SocketSingleton.getInstance().getSocket();
            } catch (URISyntaxException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }

        initEventListeners();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "EXIT, onDestroy");
        unregisterReceiver(stopServiceRec);

        if(restart) {
            Intent broadcastIntent = new Intent(this, PhoneStateServiceRestarter.class);
            broadcastIntent.setAction("infotainment.ActivityRecognition.RestartSocketListenerService");
            sendBroadcast(broadcastIntent);

        } else {
            Log.w(TAG, "Don't Restart");

        }
    }

    private void initEventListeners() {

        socket.on(SocketEvents.answerCall_event, answerCall);
        socket.on(SocketEvents.endCall_event, endCall);
        socket.on(SocketEvents.getStatus_event, getStatus);
        socket.on(SocketEvents.startPhoneCall_event, startPhoneCall);

    }

    /* ----- Socket IO events Functions ----- */
    private Emitter.Listener endCall = new Emitter.Listener() {
        private static final String TAG = "rejectCall";

        @Override
        public void call(Object... args) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @RequiresApi(api = 28)
                @Override
                public void run() {
                    String phoneNumber = (String) args[0];
                    Log.i(TAG, phoneNumber);

                    vibrate();

                /* sdk PHONE >= 28
                TelecomManager tm = (TelecomManager) Constants.context.getSystemService(Context.TELECOM_SERVICE);


                if (ActivityCompat.checkSelfPermission(Constants.context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                tm.endCall();
                */

                    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    try {
                        Class c = Class.forName(tm.getClass().getName());
                        Method m = c.getDeclaredMethod("getITelephony");
                        m.setAccessible(true);
                        ITelephony telephonyService = (ITelephony) m.invoke(tm);

                        telephonyService.silenceRinger();
                        telephonyService.endCall();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    };

    private Emitter.Listener answerCall = new Emitter.Listener() {
        private static final String TAG = "answerCall";

        @Override
        public void call(Object... args) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    String phoneNumber = (String) args[0];
                    vibrate();
                    Log.i(TAG, phoneNumber);

                    TelecomManager tm = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);


                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    tm.acceptRingingCall();

                }
            });
        }
    };

    private Emitter.Listener getStatus = new Emitter.Listener() {
        private static final String TAG = "getStatus";

        @Override
        public void call(Object... args) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    String statusJson = PhoneStatusDataBuilder.getInstance().collectStatusData(context);
                    Log.d(TAG, statusJson);

                    try {
                        SocketSingleton.sendDataToRaspberry(SocketEvents.phoneStatus_event, statusJson);

                    } catch (URISyntaxException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                }
            });
        }
    };

    private Emitter.Listener startPhoneCall = new Emitter.Listener() {
        private static final String TAG = "startPhoneCall";

        @Override
        public void call(Object... args) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    String phoneNumber = (String) args[0];
                    vibrate();
                    Log.i(TAG, phoneNumber);

                    if (!TextUtils.isEmpty(phoneNumber)) {
                        if (checkPermission(Manifest.permission.CALL_PHONE)) {

                            String dial = "tel:" + phoneNumber;
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_CALL);
                            intent.setData(Uri.parse(dial));

                            context.startActivity(intent);

                        }
                    } else {
                        Log.w(TAG, "No phone number");
                    }
                }

                private boolean checkPermission(String permission) {
                    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
                }

            });
        }
    };

    private void vibrate(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE));
        }else{
            //deprecated in API 26
            v.vibrate(500);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class UserStopServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            Log.w(TAG, "Don't restart request");
            restart = false;
        }
    }
}
