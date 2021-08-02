package com.benjaminwan.composelocation.utils

import android.content.Context
import android.location.*
import android.os.Build
import android.os.Bundle
import com.orhanobut.logger.Logger

class GpsManager(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    var minTimeMs: Long = 3000L
    var minDistanceM: Float = 1.0f

    private fun getBestProvider(): String {
        //获取定位方式
        val providers: List<String> = locationManager.getProviders(true)
        for (s in providers) {
            Logger.i(s)
        }
        val criteria = Criteria()
        // 查询精度：高，Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精确
        criteria.accuracy = Criteria.ACCURACY_FINE
        // 是否查询海拨
        criteria.isAltitudeRequired = true
        // 是否查询方位角
        criteria.isBearingRequired = false
        // 设置是否要求速度
        criteria.isSpeedRequired = true
        // 电量要求：低
        criteria.powerRequirement = Criteria.POWER_LOW
        return locationManager.getBestProvider(criteria, false) ?: "" //获取最佳定位
    }

    fun start() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMs, minDistanceM, locationListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            locationManager.registerGnssStatusCallback(gnssStatusListener)
        } else {
            locationManager.addGpsStatusListener(gpsStateListener)
        }
    }

    fun stop() {
        locationManager.removeUpdates(locationListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            locationManager.unregisterGnssStatusCallback(gnssStatusListener)
        } else {
            locationManager.removeGpsStatusListener(gpsStateListener)
        }
    }

    private val gnssStatusListener = object : GnssStatus.Callback() {
        override fun onStarted() {
            Logger.i("onStarted")
        }

        override fun onStopped() {
            Logger.i("onStopped")
        }

        override fun onFirstFix(ttffMillis: Int) {
            Logger.i("onFirstFix $ttffMillis")
        }

        override fun onSatelliteStatusChanged(status: GnssStatus) {
            Logger.i("onSatelliteStatusChanged ${status.satelliteCount}")
        }
    }

    private val gpsStateListener = object : GpsStatus.Listener {
        override fun onGpsStatusChanged(event: Int) {
            Logger.i("onGpsStatusChanged $event")
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Logger.i("latitude=${location.latitude} longitude=${location.longitude}")
            Logger.i("accuracy=${location.accuracy} altitude=${location.altitude}")
            Logger.i("bearing=${location.bearing} speed=${location.speed}")
            Logger.i("time=${location.time} elapsedRealtimeNanos=${location.elapsedRealtimeNanos}")
            Logger.i("provider=${location.provider}")
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            Logger.i("onStatusChanged $provider $status $extras")
        }

        override fun onProviderEnabled(provider: String) {
            Logger.i("onProviderEnabled $provider")
        }

        override fun onProviderDisabled(provider: String) {
            Logger.i("onProviderDisabled $provider")
        }
    }

}