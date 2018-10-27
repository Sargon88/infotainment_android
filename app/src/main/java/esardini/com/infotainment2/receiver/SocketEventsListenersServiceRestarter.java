package esardini.com.infotainment2.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import esardini.com.infotainment2.service.MainService;
import esardini.com.infotainment2.service.PhoneStateService;
import esardini.com.infotainment2.service.SocketEventsListenersService;

public class SocketEventsListenersServiceRestarter extends BroadcastReceiver {

    private static String TAG = SocketEventsListenersServiceRestarter.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Socket Service Stops!");

        SocketEventsListenersService socketEventsListenersService = new SocketEventsListenersService(context);

        MainService mService = new MainService(context);

        if(!mService.isMyServiceRunning(socketEventsListenersService.getClass(), context)){
            context.startForegroundService(new Intent(context, socketEventsListenersService.getClass()));
        }


    }
}
