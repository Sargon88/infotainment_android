package com.example.esardini.infotainment.utils;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.esardini.infotainment.Constants;

import org.json.JSONObject;

import java.util.Map;

public class DataSender {

    public static void sendDataToRaspberry(Map<String, String> params) {
        Log.i("sendDataToRaspberry", "sendDataToRasp");

        String url = "http://" + Constants.RASPBERRY + Constants.ENDPOINT;

        // POST body
        JSONObject body = new JSONObject(params);

        Log.i("sendDataToRaspberry", "URL: " + url + " - BODY: " + body.toString());

        RequestQueue volleyRequestQueue = Volley.newRequestQueue(Constants.context);

        JsonObjectRequest rqst = new JsonObjectRequest(Request.Method.POST, url, body, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                Log.i("sendDataToRaspberry", "RESPONSE: " + response);



            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("sendDataToRaspberry", error.toString());
            }
        });

        volleyRequestQueue.add(rqst);

    }


}
