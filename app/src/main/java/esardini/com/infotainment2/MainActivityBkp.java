package esardini.com.infotainment2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.net.URISyntaxException;

import esardini.com.infotainment2.constants.ErrorMessages;
import esardini.com.infotainment2.constants.InterfaceSingleton;
import esardini.com.infotainment2.service.MainService;
import esardini.com.infotainment2.service.PhoneStateService;
import esardini.com.infotainment2.service.SocketEventsListenersService;

public class MainActivityBkp extends AppCompatActivity {

    Intent psServiceIntent;
    Intent socketEventsLisIntent;
    private PhoneStateService phoneStateService;
    private SocketEventsListenersService socketEventsListeners;
    private Context context;
    private String TAG = MainActivityBkp.class.getSimpleName();
    private MainService mService;
    private InterfaceSingleton interfaceSingleton = InterfaceSingleton.getInstance();
    private Boolean connected = false;

    public Context getContext(){
        return context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        mService = new MainService(context);

        setContentView(R.layout.activity_main);

        //buttons initialization
        interfaceSingleton.setStartbtn(findViewById(R.id.startbtn));
        interfaceSingleton.setHomebtn(findViewById(R.id.homebtn));
        interfaceSingleton.setWazebtn(findViewById(R.id.wazebtn));
        interfaceSingleton.setYoutubebtn(findViewById(R.id.youtubebtn));

        interfaceSingleton.setYt_pausebtn(findViewById(R.id.yt_pause));
        interfaceSingleton.setYt_playbtn(findViewById(R.id.yt_play));
        interfaceSingleton.setYt_stopbtn(findViewById(R.id.yt_stop));

        interfaceSingleton.setWaitingBar(findViewById(R.id.waitingBar));

        mService.initializeButtons();

        Log.d(TAG, "verifyPermissions");
        Activity a = (Activity) context;

        ActivityCompat.requestPermissions(a,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                        Manifest.permission.ANSWER_PHONE_CALLS}, 1);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        connected = false;

        try {
            Boolean isConnected = mService.authenticate();

            if(isConnected){
                Log.i(TAG, "Raspbian Socket connected - Start PhoneStateService");

                connected = true;

                phoneStateService = new PhoneStateService(getContext());
                psServiceIntent = new Intent(getContext(), phoneStateService.getClass());

                if(!mService.isMyServiceRunning(phoneStateService.getClass(), context)){
                    Log.i(TAG, "Starting PhoneStateService");
                    startForegroundService(psServiceIntent);
                }


                Log.i(TAG, "Raspbian Socket connected - Start SocketEventsListenersService");
                socketEventsListeners = new SocketEventsListenersService(getContext());
                socketEventsLisIntent = new Intent(getContext(), socketEventsListeners.getClass());

                if(!mService.isMyServiceRunning(socketEventsListeners.getClass(), context)){
                    Log.i(TAG, "Starting SocketEventsListenersService");
                    startForegroundService(socketEventsLisIntent);
                }


                interfaceSingleton.getStartbtn().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Click start");

                        mService.enableDisableButtons();
                    }
                });

/* TODO da rivedere
                FloatingActionButton killThreadBtn = (FloatingActionButton) findViewById(R.id.killThreadBtn);
                killThreadBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Click KILL");

                        context.sendBroadcast(new Intent(Params.KILL_SERVICE_REQUEST));
                        stopService(psServiceIntent);
                        stopService(socketEventsLisIntent);

                        finish();

                    }
                });
*/

            } else {
                Log.e(TAG, "Can't Connect to raspbian socket");

                mService.alertMessage(ErrorMessages.errorTitle, ErrorMessages.socketConnectionError);
            }

        } catch (URISyntaxException e) {
            //errore nella connessione alla socket
            Log.e(TAG, e.getLocalizedMessage());
        }

    }

    @Override
    protected void onDestroy(){
        if(psServiceIntent != null){
            stopService(psServiceIntent);
        }

        if(socketEventsLisIntent != null) {
            stopService(socketEventsLisIntent);
        }

        Log.i(TAG, "onDestroy");

        super.onDestroy();
        /*TODO
         * verificare la parte relativa alla connessione socket
         * se la socket è connessa, mantenere il service vivo
         * se la socket è disconnessa, killare il service
         */


    }

}
