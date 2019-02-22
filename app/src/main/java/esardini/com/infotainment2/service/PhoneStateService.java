package esardini.com.infotainment2.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;

import com.google.gson.Gson;

import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

import esardini.com.infotainment2.bean.Coordinates;
import esardini.com.infotainment2.constants.Params;
import esardini.com.infotainment2.constants.SocketEvents;
import esardini.com.infotainment2.constants.SocketSingleton;
import esardini.com.infotainment2.receiver.PhoneStateReceiver;
import esardini.com.infotainment2.receiver.PhoneStateServiceRestarter;

public class PhoneStateService extends Service {

    private static String TAG= PhoneStateService.class.getSimpleName();
    private static Context c;
    private UserStopServiceReceiver stopServiceRec;

    private boolean restart = true;
    private Coordinates coordinate;

    //METODI
    public PhoneStateService(Context applicationContext){
        super();
        c = applicationContext; //sempre per primo

        Log.d(TAG, "PhoneStateService");
    }

    public PhoneStateService(){}

    @Override
    public void onCreate(){
        super.onCreate();
        startForeground(1, new Notification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);

        int count = 0;

        if(stopServiceRec == null){
            stopServiceRec = new UserStopServiceReceiver();
            registerReceiver(stopServiceRec, new IntentFilter(Params.KILL_SERVICE_REQUEST));
        }

        getGpsCoordinates();
        startCallReceiver();
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        Log.i(TAG, "EXIT, onDestroy");
        unregisterReceiver(stopServiceRec);
        stopTimerTask();

        if(restart) {
            Intent broadcastIntent = new Intent(this, PhoneStateServiceRestarter.class);
            broadcastIntent.setAction("infotainment.ActivityRecognition.RestartPhoneStateService");
            sendBroadcast(broadcastIntent);

        } else {
            Log.w(TAG, "Don't Restart");

        }

    }


    /**
     * FUNZIONALI
     * */
    private Timer timer;
    private Timer timer2;
    private TimerTask statusTask;
    private TimerTask gpsTask;
    private TimerTask tryConnectTask;

    private int gpsFrequence = Params.COORDINATES_TASK_FREQUENCE;

    public int getGpsFrequence(){
        return gpsFrequence;
    }

    public void setGpsFrequence(int i){
        gpsFrequence = i;
    }

    /* Main Function*/
    public void startTimer(){
        Log.d(TAG, "startTimer");
        timer = new Timer();
        timer2 = new Timer();

        try {
            if(!SocketSingleton.isConnected() && !Params.DEBUG){

                initializeTryConnectTask();
                timer.schedule(tryConnectTask, Params.TASK_DELAY, Params.TRY_CONNECT_TASK_FREQUENCE);

            } else{
                //è connesso alla socket

                initializeStatusTask();
                initializeGpsTask();

                Log.i(TAG, "Frequence: " + gpsFrequence);
                timer.schedule(statusTask, Params.TASK_DELAY, Params.PHONE_STATUS_TASK_FREQUENCE);
                timer2.schedule(gpsTask, Params.TASK_DELAY, gpsFrequence);
            }

        } catch (URISyntaxException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }


    }

    public void stopTimerTask(){
        Log.d(TAG, "stopTimerTask");

        if(timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
            Log.d(TAG, "Killed timer");
        }

        if(timer2 != null) {
            timer2.cancel();
            timer2.purge();
            timer2 = null;
            Log.d(TAG, "Killed timer2");
        }
    }

    public void initializeStatusTask(){
        statusTask = new TimerTask() {
            @Override
            public void run() {
                if(Looper.myLooper() == null){
                    Looper.prepare();
                }

                String statusJson = PhoneStatusDataBuilder.getInstance().collectStatusData(c);
                //String statusJson = "test 1";
                Log.d(TAG, statusJson);

                try {
                    SocketSingleton.sendDataToRaspberry(SocketEvents.phoneStatus_event, statusJson);

                } catch (URISyntaxException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }

            }
        };
    }

    public void initializeGpsTask(){
        gpsTask = new TimerTask() {
            @Override
            public void run() {

                if(Looper.myLooper() == null){
                    Looper.prepare();
                }

                if(coordinate != null){
                    Gson g = new Gson();
                    String json = g.toJson(coordinate);

                    Log.d(TAG, "Coordinate Json: " + json);

                    try {
                        SocketSingleton.getInstance().sendDataToRaspberry(SocketEvents.coordinates_event, json);
                    } catch (URISyntaxException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                }

            }
        };
    }

    public void initializeTryConnectTask(){
        tryConnectTask = new TimerTask() {
            @Override
            public void run() {

                if(Looper.myLooper() == null){
                    Looper.prepare();
                }


                try {
                    Log.i(TAG, "tryConnectTask");
                    SocketSingleton.getInstance().getSocket().connect();

                    if(SocketSingleton.isConnected()){
                        Log.i(TAG, "tryConnectTask CONNECTED");

                        stopTimerTask();
                    }

                } catch (URISyntaxException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }

            }
        };
    }
    /**
     * FINE FUNZIONALI
     */

    /**
     * FUNZIONI VARIE
     */

    //gps
    @SuppressLint("MissingPermission")
    private void getGpsCoordinates(){
        Log.i(TAG, "getGpsCoordinates");

        LocationManager lManager = (LocationManager) getSystemService(c.LOCATION_SERVICE);

        LocationListener lListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

                if(coordinate == null){
                    coordinate = new Coordinates();
                }

                coordinate.setLatitude(Double.toString(location.getLatitude()));
                coordinate.setLongitude(Double.toString(location.getLongitude()));


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }

        };

        lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, lListener);
        lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, lListener);

    }

    //call
    private void startCallReceiver(){

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        BroadcastReceiver callBReceiver = new PhoneStateReceiver();
        IntentFilter f = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        f.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        f.addAction("android.intent.action.PHONE_STATE");
        c.getApplicationContext().registerReceiver(callBReceiver, f);

    }


    /**
     * FINE FUNZIONI VARIE
     */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class UserStopServiceReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            Log.w(TAG, "Don't restart request");
            restart = false;
        }
    }
}
