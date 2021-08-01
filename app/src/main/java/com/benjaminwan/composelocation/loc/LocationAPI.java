package com.benjaminwan.composelocation.loc;

import android.content.Context;
import android.location.LocationManager;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

public class LocationAPI {
    private static HalopayLocation lastLocation;
    private Context context;
    private GpsLocationManager gpsLocationManager;
    private HalopayLocationListener listener;
    private NetworkLocationManager networkLocationManager;
    private Handler handler = new Handler();
    private int UPDATE_INTERVAL = 3000;
    private float minDistance = 1.0F;
    private List<String> allProviders = new ArrayList();

    public LocationAPI(Context c) {
        this.context = c;
        LocationManager locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
        this.allProviders = locationManager.getAllProviders();
    }

    public static HalopayLocation getLastKnownLocation() {
        return lastLocation;
    }

    public static void setLastKnownLocation(HalopayLocation loc) {
        lastLocation = loc;
    }

    public void getChangeCurLocation(HalopayLocationListener listener) {
        this.listener = listener;
        if ((this.allProviders != null) && (this.allProviders.contains(LocationManager.GPS_PROVIDER))) {
            this.gpsLocationManager = new GpsLocationManager(this.context, listener);
            this.gpsLocationManager.requestLocationUpdates(this.UPDATE_INTERVAL, this.minDistance);
        }

        if ((this.allProviders != null) && (this.allProviders.contains(LocationManager.NETWORK_PROVIDER)))
            try {
                this.networkLocationManager = new NetworkLocationManager(this.context, listener);
                this.networkLocationManager.requestLocationUpdates(this.UPDATE_INTERVAL, this.minDistance);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public GpsLocationManager getGpsLocationManager() {
        return gpsLocationManager;
    }

    public void stopGpsListener() {
        if (gpsLocationManager != null) {
            gpsLocationManager.stopListener(listener);
            gpsLocationManager.destroy();
        }
    }


}
