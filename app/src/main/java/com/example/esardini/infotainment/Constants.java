package com.example.esardini.infotainment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;

import com.example.esardini.infotainment.receiver.PhoneStateReceiver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Constants {

    public static Context context;
    public static final String RASPBERRY = "192.168.43.111";
    public static final String ENDPOINT = "/server/gate.php";
    public static final String SOCKET_PORT = "8080";
    public static io.socket.client.Socket socket;
    public static TelephonyManager telephony;
    //public static PhoneStateReceiver.MyPhoneStateListener listener;
    public static ConnectivityManager connManager;
    private static GsonBuilder gsonBuilder = new GsonBuilder();
    public static Gson gson = gsonBuilder.create();


}
