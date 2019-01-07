package esardini.com.infotainment2.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.fasterxml.jackson.databind.deser.impl.CreatorCandidate;

import esardini.com.infotainment2.constants.Params;
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
            if(!Params.KILL_ALL) {
                context.startForegroundService(new Intent(context, phoneStateService.getClass()));
            }else {
                Log.i(TAG, "KILL ALL: " + Params.KILL_ALL);
            }
        }

    }
}
