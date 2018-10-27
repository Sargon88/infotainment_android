package esardini.com.infotainment2.youtube;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;

import java.net.URISyntaxException;

import esardini.com.infotainment2.bean.VideoShareBean;
import esardini.com.infotainment2.constants.SocketSingleton;


public class ShareActivity extends Activity {

    private static final String TAG = "ShareActivity";
    private static final String youtubeUrl = "youtu";
    private static final String nodeOpenVideoEvent = "open yt video";
    public TextView tw;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            Log.d(TAG, "Single Send");

            Bundle bundle = intent.getExtras();
            if (bundle != null) {

                String subject = bundle.getString("android.intent.extra.SUBJECT");
                String text = bundle.getString("android.intent.extra.TEXT");

                if(text.contains(youtubeUrl)){

                    String[] sbjSplit = subject.split("\"");

                    VideoShareBean vsb = new VideoShareBean();
                    vsb.setUrl(text);
                    vsb.setDescription(sbjSplit[1]);

                    Gson g = new Gson();
                    String vsbJson = g.toJson(vsb);

                    Log.d(TAG, "VSBJSON: " + vsbJson);

                    io.socket.client.Socket socket = null;

/*                    try {
                        socket = IO.socket("http://" + Constants.RASPBERRY + ":" + Constants.SOCKET_PORT);

                        socket.connect();

                        socket.emit(nodeOpenVideoEvent, vsbJson);


                    } catch (URISyntaxException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    } finally {
                        socket.disconnect();
                        socket.close();
                    }
*/

                    try {
                        SocketSingleton.sendDataToRaspberry(nodeOpenVideoEvent, vsbJson);
                    } catch (URISyntaxException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                } else {
                    Log.e(TAG, "Not Youtube URL");
                }

            }


        } else if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
            Log.d(TAG, "Multiple Send");

        }

        finish();

    }


}

