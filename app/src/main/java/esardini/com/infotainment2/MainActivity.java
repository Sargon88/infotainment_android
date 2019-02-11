package esardini.com.infotainment2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.net.URISyntaxException;

import esardini.com.infotainment2.constants.InterfaceSingleton;

import esardini.com.infotainment2.constants.Params;
import esardini.com.infotainment2.constants.SocketSingleton;
import esardini.com.infotainment2.service.MainService;
import esardini.com.infotainment2.service.PhoneStateService;
import esardini.com.infotainment2.service.SocketEventsListenersService;
import esardini.com.infotainment2.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    Intent psServiceIntent;
    Intent socketEventsLisIntent;
    private PhoneStateService phoneStateService;
    private SocketEventsListenersService socketEventsListeners;
    private Context context;
    private String TAG = MainActivity.class.getSimpleName();
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

        mService = new MainService(context);
        mService.initializeParams();

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

        TextView ipView = findViewById(R.id.ip);
        ipView.setText(Params.SOCKET_ADDRESS);

        /*----- CONNECTION TASK -----*/
        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... strings) {
                Log.i(TAG, "Starting connection Task");

                mService.connectionTask();

                return null;
            }
        }.execute("");

        Log.i(TAG, "END MAIN");

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();


        Log.i(TAG, "onDestroy");

        if(psServiceIntent != null){
            stopService(psServiceIntent);
        }

        if(socketEventsLisIntent != null) {
            stopService(socketEventsLisIntent);
        }

        SocketSingleton.disconnect();



        /*TODO
         * verificare la parte relativa alla connessione socket
         * se la socket è connessa, mantenere il service vivo
         * se la socket è disconnessa, killare il service
         */


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
