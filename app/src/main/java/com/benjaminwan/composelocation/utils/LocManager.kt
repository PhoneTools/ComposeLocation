package com.benjaminwan.composelocation.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.*
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.orhanobut.logger.Logger


class LocManager(context: Context) {
    companion object {
        const val MIN_TIME_MS: Long = 1000L //更新间隔(毫秒):1000
        const val MIN_DISTANCE_M: Float = 0.0f //最小距离(当位置距离变化超过此值时更新):0
        //如果最小距离不为0，则以最小距离为准；最小距离为0，则通过时间来定时更新；两者为0，则随时刷新
    }

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var gnssStatusListener: GnssStatus.Callback? = null
    private var legacyStatusListener: GpsStatus.Listener? = null

    @SuppressLint("MissingPermission")
    @JvmOverloads
    fun startRequestLocationUpdates(minTimeMs: Long = MIN_TIME_MS, minDistanceM: Float = MIN_DISTANCE_M) {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMs, minDistanceM, locationListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val listener = gnssStatusListener()
            gnssStatusListener = listener
            locationManager.registerGnssStatusCallback(listener)
        } else {
            val listener = legacyStatusListener()
            legacyStatusListener = listener
            locationManager.addGpsStatusListener(listener)
        }
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        locationManager.removeUpdates(locationListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val listener = gnssStatusListener
            if (listener != null) {
                locationManager.unregisterGnssStatusCallback(listener)
            }
        } else {
            val listener = legacyStatusListener
            if (listener != null) {
                locationManager.removeGpsStatusListener(listener)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun gnssStatusListener() = object : GnssStatus.Callback() {
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
            Logger.i("onSatelliteStatusChanged")
            (0..status.satelliteCount).forEach {
                val svid = status.getSvid(it)
                val constellationType = status.getConstellationType(it)
                val type = when (constellationType) {
                    GnssStatus.CONSTELLATION_UNKNOWN -> "UNKNOWN"
                    GnssStatus.CONSTELLATION_GPS -> "GPS"
                    GnssStatus.CONSTELLATION_SBAS -> "SBAS"
                    GnssStatus.CONSTELLATION_GLONASS -> "GLONASS"
                    GnssStatus.CONSTELLATION_QZSS -> "QZSS"
                    GnssStatus.CONSTELLATION_BEIDOU -> "BEIDOU"
                    GnssStatus.CONSTELLATION_GALILEO -> "GALILEO"
                    GnssStatus.CONSTELLATION_IRNSS -> "IRNSS"
                    else -> Integer.toString(constellationType)
                }
                val cn0DbHz = status.getCn0DbHz(it)
                val getElevationDegrees = status.getElevationDegrees(it)
                val azimuthDegrees = status.getAzimuthDegrees(it)
                val hasEphemerisData = status.hasEphemerisData(it)
                val hasAlmanacData = status.hasAlmanacData(it)
                val usedInFix = status.usedInFix(it)
                val hasCarrierFrequencyHz = status.hasCarrierFrequencyHz(it)
                val carrierFrequencyHz = status.getCarrierFrequencyHz(it)
                val hasBasebandCn0DbHz = status.hasBasebandCn0DbHz(it)
                val basebandCn0DbHz = status.getBasebandCn0DbHz(it)
            }
        }
    }

    private var mLegacyStatus: GpsStatus? = null

    @SuppressLint("MissingPermission")
    private fun legacyStatusListener() = object : GpsStatus.Listener {
        override fun onGpsStatusChanged(event: Int) {
            Logger.i("onGpsStatusChanged $event")
            when (event) {
                GpsStatus.GPS_EVENT_STARTED -> {
                    Logger.i("GPS_EVENT_STARTED")
                }
                GpsStatus.GPS_EVENT_STOPPED -> {
                    Logger.i("GPS_EVENT_STOPPED")
                }
                GpsStatus.GPS_EVENT_FIRST_FIX -> {
                    Logger.i("GPS_EVENT_FIRST_FIX")
                }
                GpsStatus.GPS_EVENT_SATELLITE_STATUS -> {
                    Logger.i("GPS_EVENT_SATELLITE_STATUS")
                    mLegacyStatus = locationManager.getGpsStatus(mLegacyStatus)
                    val gpsStatus = mLegacyStatus
                    if (gpsStatus != null) {
                        // 获取卫星颗数的默认最大值
                        val maxSatellites = gpsStatus.maxSatellites
                        Logger.i("maxSatellites=$maxSatellites")
                        // 创建一个迭代器保存所有卫星
                        gpsStatus?.satellites?.forEach {
                            Logger.i("$it")
                        }
                        val iters: Iterator<GpsSatellite> = gpsStatus.satellites.iterator()
                        var count = 0
                        while (iters.hasNext() && count <= maxSatellites) {
                            val s = iters.next()
                            Logger.i("$s")
                            count++
                        }
                    }
                }
            }
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