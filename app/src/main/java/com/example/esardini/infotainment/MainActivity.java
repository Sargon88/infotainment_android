package com.example.esardini.infotainment;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.internal.telephony.ITelephony;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.esardini.infotainment.receiver.PhoneStateReceiver;
import com.example.esardini.infotainment.service.CallPageService;
import com.example.esardini.infotainment.utils.WazeLocationListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.emitter.Emitter;

import static com.example.esardini.infotainment.Constants.ENDPOINT;
import static com.example.esardini.infotainment.Constants.RASPBERRY;
import static com.example.esardini.infotainment.Constants.context;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private String HASH = null;
    private String IP = null;
    public static Integer mSignalStrength = 0;
    private static Integer batteryLevel = 0;
    private static WazeLocationListener listener;
    private FloatingActionButton startbtn;
    private FloatingActionButton homebtn;
    private FloatingActionButton youtubebtn;
    private FloatingActionButton wazebtn;
    private static Button yt_playbtn;
    private static Button yt_pausebtn;
    private static Button yt_stopbtn;
    private Boolean shown = false;
    private Boolean ytShown = false;
    private Boolean connected = false;
    private static String latitude = "";
    private static String longitude = "";
    public static TextView tw;
    private static CallPageService cpService = new CallPageService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        Log.i("BLUE-INFO", "STARTED");
        setContentView(R.layout.activity_main);

        startbtn = findViewById(R.id.startbtn);
        homebtn = findViewById(R.id.homebtn);
        wazebtn = findViewById(R.id.wazebtn);
        youtubebtn = findViewById(R.id.youtubebtn);

        //bottoni youtube
        yt_playbtn = findViewById(R.id.yt_play);
        yt_pausebtn = findViewById(R.id.yt_pause);
        yt_stopbtn = findViewById(R.id.yt_stop);

        homebtn.setVisibility(View.GONE);
        wazebtn.setVisibility(View.GONE);
        youtubebtn.setVisibility(View.GONE);

        yt_playbtn.setVisibility(View.GONE);
        yt_pausebtn.setVisibility(View.GONE);
        yt_stopbtn.setVisibility(View.GONE);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                        Manifest.permission.ANSWER_PHONE_CALLS}, 1);


        connected = false;

        //faccio l'autenticazione
        this.authenticate();

    }

    protected void onDestroy() {
        super.onDestroy();

        ComponentName component = new ComponentName(context, PhoneStateReceiver.class);
        Constants.context.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED , PackageManager.DONT_KILL_APP);

    }

    //FUNCTIONS
    private Boolean authenticate() {
        Log.i("BLUE-INFO", "authenticate");
        String ip = getIPAddress();

        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "authenticate");
        params.put("ip", ip);

        doAuthenticate(params);

        return true;
    }


    public BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);

            batteryLevel = level;
            collectStatusData();

        }
    };

    public static void collectStatusData() {
        Boolean wifiStatus = false;
        Boolean bluetoothStatus = false;
        String lineStatus = "";

        //Bluetooth
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            bluetoothStatus = false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable :)
                bluetoothStatus = false;
            } else {
                bluetoothStatus = true;
            }
        }

        //wi-fi
        NetworkInfo wifiCheck = Constants.connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);


        if (wifiCheck.isConnectedOrConnecting()) {
            //wifi connected
            wifiStatus = true;
        } else {
            //wifi not connected
            wifiStatus = false;
        }

        /* Contacts page */
        String starredContacts = cpService.getStarredContactsJSON();


        String lastCallsJson = cpService.getLastCallsJSON();
        Log.i("aaa", lastCallsJson);

        String params = "{";
        params += "\"esito\": true, ";
        params += "\"phonestat\":{";
        params += "\"batt\": \"" + batteryLevel.toString() + "\"";
        params += ", \"wifi\": \"" + wifiStatus.toString() + "\"";
        params += ", \"bluetooth\": \"" + bluetoothStatus.toString() + "\"";
        params += ", \"signal\": \"" + mSignalStrength.toString() + "\"";
        params += ", \"starredcontacts\": " + starredContacts;
        params += ", \"lastcalls\": " + lastCallsJson;
        params += ", \"latitude\": \"" + latitude + "\"";
        params += ", \"longitude\": \"" + longitude + "\"}}";

        Log.i(TAG + "-INFO", "STATUS: " + params);

        sendDataToRaspberry("phone status", params);
    }

    /* Main function */
    private void doAuthenticate(Map<String, String> params) {
        String url = "http://" + RASPBERRY + ENDPOINT;

        // POST body
        JSONObject body = new JSONObject(params);

        Log.i("BLUE-INFO", "URL: " + url + " - BODY: " + body.toString());

        RequestQueue volleyRequestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest rqst = new JsonObjectRequest(Request.Method.POST, url, body, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try {
                    if (response.getBoolean("esito")) {

                        //Socket creation
                        try {

                            Constants.socket = IO.socket("http://" + Constants.RASPBERRY + ":" + Constants.SOCKET_PORT);
                            Constants.socket.connect();

                            initEventListeners();

                        } catch (URISyntaxException e) {
                            Log.e(TAG, e.getLocalizedMessage());
                        }


                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }

                        Constants.connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                        HASH = response.getString("hash");
                        IP = params.get("ip");

                        Log.i("BLUE-INFO", "Connected: " + HASH);
                        connected = true;

                        /* estraggo il livello di carica della batteria */
                        context.registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

                        /* GPS */
                        LocationManager locationManager = (LocationManager) getSystemService(context.LOCATION_SERVICE);


                        LocationListener lListener = new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                Log.i("BLUE-INFO", "COO longitude: " + location.getLongitude());
                                Log.i("BLUE-INFO", "COO latitude: " + location.getLatitude());

                                TextView longV = (TextView) findViewById(R.id.longitude);
                                TextView latV = (TextView) findViewById(R.id.latitude);

                                longitude = Double.toString(location.getLongitude());
                                latitude = Double.toString(location.getLatitude());

                                longV.setText(String.valueOf(longitude));
                                latV.setText(String.valueOf(latitude));

                            }

                            @Override
                            public void onStatusChanged(String s, int i, Bundle bundle) {
                            }

                            @Override
                            public void onProviderEnabled(String s) {
                            }

                            @Override
                            public void onProviderDisabled(String s) {
                            }
                        };

                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, lListener);
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, lListener);
                        /* GPS */

                        /* CALL */
                        BroadcastReceiver callBReceiver = new PhoneStateReceiver();
                        IntentFilter f = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
                        f.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
                        f.addAction("android.intent.action.PHONE_STATE");
                        context.registerReceiver(callBReceiver, f);
                        /* CALL */


                        startbtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.i("BLUE-INFO", "Click start - shown: " + shown);

                                enableDisableButtons();

                            }
                        });



                    } else {
                        Log.e("BLUE-ERROR", response.getString("msg"));

                        alertMessage("Errore", response.getString("msg"));

                    }
                } catch (JSONException e) {
                    Log.e(TAG + "-ERROR", e.getLocalizedMessage());
                    alertMessage("Errore", e.getMessage());

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.toString().equals("com.android.volley.TimeoutError")) {
                    Log.w("BLUR-WARN", error.toString());
                    if (!connected) {
                        authenticate();
                    }
                } else {
                    Log.e("BLUE-ERROR", error.toString());
                    alertMessage("Attenzione", "Attivare il router wifi");
                }
            }
        });

        volleyRequestQueue.add(rqst);

    }

    private void activateButtons() {

        homebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("BLUE-INFO", "Home button");

                disableYTButton();

                Map<String, String> params = new HashMap<String, String>();
                params.put("action", "changepage");
                params.put("page", "home");

                changePageRequest(params);
                listener = null;

            }
        });

        youtubebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("BLUE-INFO", "YouTube button");

                enableYTButton();

                loadYTPage();

                Map<String, String> params = new HashMap<String, String>();
                params.put("action", "changepage");
                params.put("page", "yt");

                changePageRequest(params);
                listener = null;

            }
        });

        wazebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("BLUE-INFO", "Waze button");

                disableYTButton();

                sendDataToRaspberry("change page", "map");


            }
        });


    }

    private void changePageRequest(Map<String, String> params) {
        String url = "http://" + RASPBERRY + ENDPOINT;

        // POST body
        JSONObject body = new JSONObject(params);

        Log.i("BLUE-INFO", "URL: " + url + " - BODY: " + body.toString());

        RequestQueue volleyRequestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest rqst = new JsonObjectRequest(Request.Method.POST, url, body, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try {
                    if (response.getBoolean("esito")) {

                        Log.i("BLUE-INFO", "RESPONSE: " + response.toString());

                        enableDisableButtons();

                    } else {

                        Log.e("BLUE-ERROR", "Impossibile cambiare pagina");

                    }
                } catch (JSONException e) {
                    Log.e("BLUE - ERROR", e.getLocalizedMessage());
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e("BLUE-ERROR", error.getLocalizedMessage());
                alertMessage("Attenzione", "Impossibile cambiare pagina");
                enableDisableButtons();
            }
        });

        volleyRequestQueue.add(rqst);

    }

    public static void sendDataToRaspberry(String action, String params) {
        Log.i(TAG + "-INFO", "sending data");

        if(!Constants.socket.connected()){
            Constants.socket.connect();
        }

        Constants.socket.emit(action, params);

    }

    private void disableYTButton() {

        yt_playbtn.setVisibility(View.GONE);
        yt_pausebtn.setVisibility(View.GONE);
        yt_stopbtn.setVisibility(View.GONE);

        yt_playbtn.setOnClickListener(null);
        yt_pausebtn.setOnClickListener(null);
        yt_stopbtn.setOnClickListener(null);

        tw = ((Activity) Constants.context).findViewById(R.id.yt_title);
        tw.setText("");

    }

    public static void enableYTButton() {

        yt_playbtn.setVisibility(View.VISIBLE);
        yt_pausebtn.setVisibility(View.VISIBLE);
        yt_stopbtn.setVisibility(View.VISIBLE);

        activateYTButtons();

    }

    private static void activateYTButtons() {

        yt_playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("BLUE-INFO", "Play button");

                sendDataToRaspberry("ytcommand", "play");
                listener = null;

            }
        });

        yt_pausebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("BLUE-INFO", "Pause button");

                sendDataToRaspberry("ytcommand", "pause");
                listener = null;

            }
        });

        yt_stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("BLUE-INFO", "Stop button");

                sendDataToRaspberry("ytcommand", "stop");
                listener = null;

            }
        });

    }

    private void alertMessage(String title, String msg) {
        Log.i("BLUE_INFO", "UPDATE VIEW: " + msg);

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

    public static String getIPAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;
                        if (isIPv4) {
                            return sAddr;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        } // for now eat exceptions
        return "";
    }

    private void loadYTPage() {

    }

    private void updateView(String msg) {
        Log.i("BLUE_INFO", "UPDATE VIEW: " + msg);

        TextView rec = (TextView) findViewById(R.id.receiver);
        rec.setText(String.valueOf(msg));

    }

    private void enableDisableButtons() {

        if (shown) {

            homebtn.setVisibility(View.GONE);
            youtubebtn.setVisibility(View.GONE);
            wazebtn.setVisibility(View.GONE);

        } else {

            homebtn.setVisibility(View.VISIBLE);
            youtubebtn.setVisibility(View.VISIBLE);
            wazebtn.setVisibility(View.VISIBLE);

            activateButtons();

        }

        shown = !shown;

    }

    private void initEventListeners() {

        Constants.socket.on("answer call", answerCall);
        Constants.socket.on("end call", endCall);
        Constants.socket.on("getStatus", getStatus);
        Constants.socket.on("start phone call", startPhoneCall);

    }

    /* ----- Socket IO events Functions ----- */
    private Emitter.Listener endCall = new Emitter.Listener() {
        private static final String TAG = "rejectCall";

        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {

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
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    String phoneNumber = (String) args[0];
                    vibrate();
                    Log.i(TAG, phoneNumber);

                    TelecomManager tm = (TelecomManager) Constants.context.getSystemService(Context.TELECOM_SERVICE);


                    if (ActivityCompat.checkSelfPermission(Constants.context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
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
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    collectStatusData();
                }
            });
        }
    };

    private Emitter.Listener startPhoneCall = new Emitter.Listener() {
        private static final String TAG = "startPhoneCall";

        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {

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

                            Constants.context.startActivity(intent);
                        }
                    } else {
                        Log.w(TAG, "No phone number");
                    }
                }

                private boolean checkPermission(String permission) {
                    return ContextCompat.checkSelfPermission(Constants.context, permission) == PackageManager.PERMISSION_GRANTED;
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

}//class


