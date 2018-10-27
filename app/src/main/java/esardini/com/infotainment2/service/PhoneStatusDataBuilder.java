package esardini.com.infotainment2.service;


import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.util.Log;

import com.google.gson.Gson;

import esardini.com.infotainment2.bean.PhoneStatusData;

public class PhoneStatusDataBuilder {

    private static final String TAG = PhoneStatusDataBuilder.class.getSimpleName();
    private PhoneStatusData psd;
    CallPageService cpService = new CallPageService();

    private static PhoneStatusDataBuilder instance = null;

    private PhoneStatusDataBuilder(){}

    public static PhoneStatusDataBuilder getInstance(){
        if(instance == null){
            instance = new PhoneStatusDataBuilder();
        }

        return instance;
    }

    public String collectStatusData(Context c){

        updateStatusData(c);

        Gson g = new Gson();
        String json = g.toJson(psd);

        Log.d(TAG, "status: " + json);

        return json;
    }

    public void updateStatusData(Context c){

        if(psd == null) {
            psd = new PhoneStatusData();
        }

        psd.setBatt(getBatteryLevel(c)+"");
        psd.setBluetooth(getBluetoothStatus());
        psd.setWifi(getWifiStatus(c));
        psd.setSignal("0");
        if(psd.getStarredcontacts() == null){
            psd.setStarredcontacts(cpService.getStarredContacts(c));
        }
        psd.setLastcalls(cpService.getLastCalls(c));

    }

    //batteria
    private int getBatteryLevel(Context c){
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = c.registerReceiver(null, ifilter);


        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return Math.round((level/(float) scale)*100);

    }

    //bluetooth
    private boolean getBluetoothStatus(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Boolean bluetoothStatus = false;

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

        return bluetoothStatus;
    }

    //wifi
    private boolean getWifiStatus(Context c){
        ConnectivityManager connManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);;
        NetworkInfo wifiCheck = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean wifiStatus = false;


        if (wifiCheck.isConnectedOrConnecting()) {
            //wifi connected
            wifiStatus = true;
        } else {
            //wifi not connected
            wifiStatus = false;
        }

        return wifiStatus;
    }

}
