package com.benjaminwan.composelocation.loc;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

public class NetworkLocationManager extends BaseLocationManager {
    private HalopayLocationListener listener;
    private LocationManager mLocationManager;

    public NetworkLocationManager(Context context, HalopayLocationListener listener) {
        this.listener = listener;
        this.mLocationManager = ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
    }

    @Override
    public void requestLocationUpdates(long minTime, float minDistance) {
        if (this.mLocationManager != null) {
            this.mLocationManager.removeUpdates(this);
            this.mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, this);
        }
    }

    public void stopListner(HalopayLocationListener l) {
        if ((this.listener != null) && (l == this.listener))
            this.listener = null;
    }

    public void destroy() {
        if (this.mLocationManager != null)
            this.mLocationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if ((this.listener != null) && (location != null)) {
            HalopayLocation ycLocation = new HalopayLocation(location);
            ycLocation.setDataSource(10003);
            ycLocation.setProvider("network");
            ycLocation.setRadius(location.getAccuracy());
            ycLocation.setCoordinateSystem("world");
            LocationAPI.setLastKnownLocation(ycLocation);
            this.listener.onLocationChanged(ycLocation);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (this.listener != null)
            this.listener.onProviderDisabled(provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (this.listener != null)
            this.listener.onProviderEnabled(provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (this.listener != null)
            this.listener.onStatusChanged(provider, status, extras);
    }
}
