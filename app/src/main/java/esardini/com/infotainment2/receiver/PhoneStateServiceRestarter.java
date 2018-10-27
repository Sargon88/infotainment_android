package esardini.com.infotainment2.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import esardini.com.infotainment2.service.MainService;
import esardini.com.infotainment2.service.PhoneStateService;

public class PhoneStateServiceRestarter extends BroadcastReceiver {

    private static String TAG = PhoneStateServiceRestarter.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Service Stops!");

        PhoneStateService phoneStateService = new PhoneStateService(context);

        MainService mService = new MainService(context);

        if(!mService.isMyServiceRunning(phoneStateService.getClass(), context)){
            context.startForegroundService(new Intent(context, phoneStateService.getClass()));
        }

    }
}
