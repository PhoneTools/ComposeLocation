package com.benjaminwan.composelocation.loc;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public abstract class BaseLocationManager implements LocationListener, ILocationManager {
    public void requestLocationUpdates(long minTime, float minDistance) {
    }

    public void onLocationChanged(Location location) {
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}
