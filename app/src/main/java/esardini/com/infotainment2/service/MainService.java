package esardini.com.infotainment2.service;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import esardini.com.infotainment2.R;
import esardini.com.infotainment2.constants.ErrorMessages;
import esardini.com.infotainment2.constants.InterfaceSingleton;
import esardini.com.infotainment2.constants.Params;
import esardini.com.infotainment2.constants.SocketEvents;
import esardini.com.infotainment2.constants.SocketSingleton;
import esardini.com.infotainment2.settings.SettingsActivity;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainService {
    private static String TAG = MainService.class.getSimpleName();
    private boolean shown = false;
    private Context context;
    Intent psServiceIntent;
    Intent socketEventsLisIntent;
    private PhoneStateService phoneStateService;
    private SocketEventsListenersService socketEventsListeners;
    private Boolean scanning = true;
    private Integer ipIndex = 0;
    private Integer connectionRetry = 1;

    public MainService(Context c){
        context = c;
    }

    public boolean isMyServiceRunning(Class<?> serviceClass, Context c){
        ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);

        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if(serviceClass.getName().equals(service.service.getClassName())){

                Log.i(TAG,"is " + serviceClass.getName() + " Running? "+ true);
                return true;
            }
        }

        Log.i(TAG,"is " + serviceClass.getName() + " Running? "+false);
        return false;
    }

    public void connectionTask(){
        try {
            Socket socket = SocketSingleton.getInstance().getSocket();


            Log.d(TAG, "ON Socket Default Events");
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    try{

                        Log.i(TAG, "SOCKET CONNECTED");
                        SocketSingleton.getInstance().setConnected(true);
                        Params.KILL_ALL = false;
                        connectionRetry = 1;

                        ((Activity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                InterfaceSingleton.getInstance().getWaitingBar().setVisibility(View.GONE);
                                InterfaceSingleton.getInstance().getStartbtn().setVisibility(View.VISIBLE);

                                InterfaceSingleton.getInstance().getStartbtn().setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Log.d(TAG, "Click start");

                                        enableDisableButtons();
                                    }
                                });

                            }
                        });

                        startBgServices();

                    } catch(NullPointerException n){
                        Log.e(TAG, "Non esiste l'interfaccia");
                    } catch (URISyntaxException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }


                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.i(TAG, "SOCKET DISCONNECTED");
                }
            }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        Log.i(TAG, "SOCKET CONNECTION ERROR");

                        socketConnectionErrorAction();
                    } catch (URISyntaxException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                }
            }).on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        Log.i(TAG, "SOCKET CONNECTION TIMEOUT");

                        socketConnectionErrorAction();
                    } catch (URISyntaxException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                }
            });

            SocketSingleton.connect();

        } catch (URISyntaxException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }

    }

    private void socketConnectionErrorAction() throws URISyntaxException {
        Log.i(TAG, "socketConnectionErrorAction");
        ipIndex++;

        if(ipIndex < Params.IPLIST.length) {
            SocketSingleton.getInstance().configureSocket(ipIndex);
            SocketSingleton.connect();
        } else {
            Log.e(TAG, "Impossibile connettersi a nessuno degli ip salvati: " + Params.RASPBERRY);

            Log.i(TAG, "RETRY: " + connectionRetry + " on " + Params.MAX_CONNECTION_RETRY);

            if (connectionRetry >= Params.MAX_CONNECTION_RETRY) {

                Params.KILL_ALL = true;

                killServices();
            }
            connectionRetry++;

        }
    }

    public void initializeButtons(){
        Log.d(TAG, "initializeButtons");

        InterfaceSingleton.getInstance().getStartbtn().setVisibility(View.GONE);
        InterfaceSingleton.getInstance().getWaitingBar().setVisibility(View.VISIBLE);

        hideFunctionButtons();
        Log.d(TAG, "hideFunctionButtons");

        hideYoutubeButtons();
        Log.d(TAG, "hideYoutubeButtons");

    }

    private void hideFunctionButtons(){
        InterfaceSingleton.getInstance().getHomebtn().setVisibility(View.GONE);
        InterfaceSingleton.getInstance().getWazebtn().setVisibility(View.GONE);
        InterfaceSingleton.getInstance().getYoutubebtn().setVisibility(View.GONE);

    }

    private void showFunctionButtons(){
        InterfaceSingleton.getInstance().getHomebtn().setVisibility(View.VISIBLE);
        InterfaceSingleton.getInstance().getWazebtn().setVisibility(View.VISIBLE);
        InterfaceSingleton.getInstance().getYoutubebtn().setVisibility(View.VISIBLE);

    }

    private void hideYoutubeButtons(){
        InterfaceSingleton.getInstance().getYt_playbtn().setVisibility(View.GONE);
        InterfaceSingleton.getInstance().getYt_pausebtn().setVisibility(View.GONE);
        InterfaceSingleton.getInstance().getYt_stopbtn().setVisibility(View.GONE);
    }

    public void verifyPermissions(Context c){
        Log.d(TAG, "verifyPermissions");
        Activity a = (Activity) c;

        ActivityCompat.requestPermissions(a,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                        Manifest.permission.ANSWER_PHONE_CALLS}, 1);

        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(c, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    public boolean authenticate() throws URISyntaxException {
        Log.i(TAG, "authenticate");
        Map<String, String> params = new HashMap<String, String>();

/*
        String ip = getIPAddress();
        params.put("action", "authenticate");
        params.put("ip", ip);
*/
        return doAuthenticate(params);

    }

    @Deprecated
    private boolean doAuthenticate(Map<String, String> params) throws URISyntaxException {
        Log.i(TAG, "doAuthenticate");

        /*
            TODO rivalutare la parte relativa all'autenticazione
         */

        SocketSingleton socket = SocketSingleton.getInstance();

        if(socket.getSocket() == null){
            Log.e(TAG, "SOCKET NULL!!!!");
        } else {
            Log.e(TAG, "SOCKET NON NULL!!!!");
        }

        Log.d(TAG, "is Connected? " + SocketSingleton.isConnected());
        Log.d(TAG, "is DEBUG? " + Params.DEBUG);

        //provo a connettere 3 volte
        int count = 0;


        while(!SocketSingleton.isConnected() && count < 3){
            try {

                TimeUnit.SECONDS.sleep(5*count);
                Log.d(TAG, "Connect try: " + count);
                SocketSingleton.connect();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(!SocketSingleton.isConnected() && !Params.DEBUG){
            Log.d(TAG, "Can't reach the socket & NOT debug");

            return false;

        }

        return true;
    }

    public void enableDisableButtons(){
        Log.d(TAG, "shown: " + shown);

        if (shown) {
            hideFunctionButtons();

        } else {
            showFunctionButtons();

            activateButtons();

        }

        shown = !shown;

    }

    private void activateButtons() {

        InterfaceSingleton.getInstance().getHomebtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Home button");

                disableYTButton();

                try {
                    changePageRequest("home");

                } catch (URISyntaxException e) {
                    Log.e(TAG, e.getLocalizedMessage());

                    alertMessage(ErrorMessages.errorTitle, ErrorMessages.changePageError);

                }

            }
        });

        InterfaceSingleton.getInstance().getYoutubebtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("BLUE-INFO", "YouTube button");

                enableYTButton();

                try {
                    changePageRequest("yt");

                } catch (URISyntaxException e) {
                    Log.e(TAG, e.getLocalizedMessage());

                    alertMessage(ErrorMessages.errorTitle, ErrorMessages.changePageError);

                }

            }
        });

        InterfaceSingleton.getInstance().getWazebtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("BLUE-INFO", "Waze button");

                disableYTButton();

                try {
                    changePageRequest("map");

                } catch (URISyntaxException e) {
                    Log.e(TAG, e.getLocalizedMessage());

                    alertMessage(ErrorMessages.errorTitle, ErrorMessages.changePageError);

                }


            }
        });


    }

    private void disableYTButton() {

        InterfaceSingleton.getInstance().getYt_playbtn().setVisibility(View.GONE);
        InterfaceSingleton.getInstance().getYt_pausebtn().setVisibility(View.GONE);
        InterfaceSingleton.getInstance().getYt_stopbtn().setVisibility(View.GONE);

        InterfaceSingleton.getInstance().getYt_playbtn().setOnClickListener(null);
        InterfaceSingleton.getInstance().getYt_pausebtn().setOnClickListener(null);
        InterfaceSingleton.getInstance().getYt_stopbtn().setOnClickListener(null);

        TextView tw = ((Activity) context).findViewById(R.id.yt_title);
        tw.setText("");

    }

    public void enableYTButton() {

        InterfaceSingleton.getInstance().getYt_playbtn().setVisibility(View.VISIBLE);
        InterfaceSingleton.getInstance().getYt_pausebtn().setVisibility(View.VISIBLE);
        InterfaceSingleton.getInstance().getYt_stopbtn().setVisibility(View.VISIBLE);

        activateYTButtons();

    }

    private void activateYTButtons() {

        InterfaceSingleton.getInstance().getYt_playbtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Play button");

                try {
                    SocketSingleton.getInstance().sendDataToRaspberry(SocketEvents.omxcommand_event, "pause");

                } catch (URISyntaxException e) {
                    Log.e(TAG, e.getLocalizedMessage());

                    alertMessage(ErrorMessages.errorTitle, ErrorMessages.yt_commandError);
                }

            }
        });

        InterfaceSingleton.getInstance().getYt_pausebtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Pause button");

                try {
                    SocketSingleton.getInstance().sendDataToRaspberry(SocketEvents.omxcommand_event, "pause");

                } catch (URISyntaxException e) {
                    Log.e(TAG, e.getLocalizedMessage());

                    alertMessage(ErrorMessages.errorTitle, ErrorMessages.yt_commandError);
                }

            }
        });

        InterfaceSingleton.getInstance().getYt_stopbtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Stop button");

                try {
                    SocketSingleton.getInstance().sendDataToRaspberry(SocketEvents.omxcommand_event, "stop");

                } catch (URISyntaxException e) {
                    Log.e(TAG, e.getLocalizedMessage());

                    alertMessage(ErrorMessages.errorTitle, ErrorMessages.yt_commandError);
                }

            }
        });

    }

    private void changePageRequest(String page) throws URISyntaxException {
        Log.d(TAG, "changePageRequest: " + page);
        SocketSingleton.getInstance().sendDataToRaspberry(SocketEvents.changePage_event, page);

    }

    public void alertMessage(String title, String msg) {
        Log.i(TAG, "UPDATE VIEW: " + msg);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder.setTitle(title);

        alertDialogBuilder
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public void startBgServices(){
        Log.i(TAG, "Raspbian Socket connected - Start PhoneStateService");

        phoneStateService = new PhoneStateService(context);
        psServiceIntent = new Intent(context, phoneStateService.getClass());

        if(!isMyServiceRunning(phoneStateService.getClass(), context)){
            Log.i(TAG, "Starting PhoneStateService");
            context.startForegroundService(psServiceIntent);
        }


        Log.i(TAG, "Raspbian Socket connected - Start SocketEventsListenersService");
        socketEventsListeners = new SocketEventsListenersService(context);
        socketEventsLisIntent = new Intent(context, socketEventsListeners.getClass());

        if(!isMyServiceRunning(socketEventsListeners.getClass(), context)){
            Log.i(TAG, "Starting SocketEventsListenersService");
            context.startForegroundService(socketEventsLisIntent);
        }
    }

    private void killServices(){
        Intent mainServiceIntent = new Intent(context, MainService.class);
        context.stopService(mainServiceIntent);

        Intent phoneStateServiceIntent = new Intent(context, PhoneStateService.class);
        context.stopService(phoneStateServiceIntent);


        try {
            SocketSingleton.getInstance().getSocket().close();
        } catch (URISyntaxException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void initializeParams() {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        String ip = sharedPref.getString(SettingsActivity.KEY_RASPBERRY_IP,"Ip Here");
        String port = sharedPref.getString(SettingsActivity.KEY_RASPBERRY_PORT,"Port Here");
        String status = sharedPref.getString(SettingsActivity.KEY_PHONE_STATUS_TASK_FREQUENCE,"20000");
        String coordinates = sharedPref.getString(SettingsActivity.KEY_COORDINATES_TASK_FREQUENCE,"1000");
        String reconnect = sharedPref.getString(SettingsActivity.KEY_TRY_CONNECT_TASK_FREQUENCE,"30000");
        String delay = sharedPref.getString(SettingsActivity.KEY_TASK_DELAY,"1000");
        String retry = sharedPref.getString(SettingsActivity.KEY_MAX_CONN,"20");


        Params.RASPBERRY = ip;
        Params.SOCKET_PORT = port;
        Params.SOCKET_ADDRESS = "http://"+ ip + ":" + port;
        Params.PHONE_STATUS_TASK_FREQUENCE = Integer.parseInt(status);
        Params.COORDINATES_TASK_FREQUENCE = Integer.parseInt(coordinates);
        Params.TRY_CONNECT_TASK_FREQUENCE = Integer.parseInt(reconnect);
        Params.TASK_DELAY = Integer.parseInt(delay);
        Params.MAX_CONNECTION_RETRY = Integer.parseInt(retry);

    }

}
