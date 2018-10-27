package com.example.esardini.infotainment.utils;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class WazeLocationListener implements LocationListener {


    @Override
    public void onLocationChanged(Location location) {

        Log.i("BLUE_COO", "Longitude: " + location.getLongitude());
        Log.i("BLUE_COO", "Latitude: " + location.getLatitude());

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
