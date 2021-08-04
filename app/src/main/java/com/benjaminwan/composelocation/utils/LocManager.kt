package com.benjaminwan.composelocation.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.*
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.orhanobut.logger.Logger
import kotlinx.coroutines.flow.MutableStateFlow


class LocManager(context: Context) {
    companion object {
        const val MIN_TIME_MS: Long = 1000L //更新间隔(毫秒):1000
        const val MIN_DISTANCE_M: Float = 0.0f //最小距离(当位置距离变化超过此值时更新):0
        //如果最小距离不为0，则以最小距离为准；最小距离为0，则通过时间来定时更新；两者为0，则随时刷新
    }

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var locationListener: LocationListener? = null

    private var legacyStatusListener: GpsStatus.Listener? = null
    private var legacyNmeaListener: GpsStatus.NmeaListener? = null

    private var gnssStatusListener: GnssStatus.Callback? = null
    private var onNmeaMessageListener: OnNmeaMessageListener? = null

    val satelliteStateFlow = MutableStateFlow<List<Satellite>>(emptyList())
    val NmeaStateFlow = MutableStateFlow<Nmea>(Nmea())
    val locationStateFlow = MutableStateFlow<Location>(Location(LocationManager.GPS_PROVIDER))

    @SuppressLint("MissingPermission")
    @JvmOverloads
    fun start(minTimeMs: Long = MIN_TIME_MS, minDistanceM: Float = MIN_DISTANCE_M) {
        locationListener().let {
            locationListener = it
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMs, minDistanceM, it)
        }
        addStatusListener()
        addNmeaListener()
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        locationListener?.let {
            locationManager.removeUpdates(it)
        }
        removeStatusListener()
        removeNmeaListener()
    }

    @SuppressLint("MissingPermission")
    private fun addStatusListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            gnssStatusListener().let {
                gnssStatusListener = it
                locationManager.registerGnssStatusCallback(it)
            }
        } else {
            legacyStatusListener().let {
                legacyStatusListener = it
                locationManager.addGpsStatusListener(it)
            }
        }
    }

    private fun removeStatusListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            gnssStatusListener?.let {
                locationManager.unregisterGnssStatusCallback(it)
            }
        } else {
            legacyStatusListener?.let {
                locationManager.removeGpsStatusListener(it)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun addNmeaListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            onNmeaMessageListener().let {
                onNmeaMessageListener = it
                locationManager.addNmeaListener(it)
            }
        } else {
            legacyNmeaListener().let {
                legacyNmeaListener = it
                locationManager.addNmeaListener(it)
            }
        }
    }

    private fun removeNmeaListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            onNmeaMessageListener?.let {
                locationManager.removeNmeaListener(it)
            }
        } else {
            legacyNmeaListener?.let {
                locationManager.removeNmeaListener(it)
            }
        }
    }

    private fun locationListener() = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Logger.i("location = $location")
            Logger.i("latitude=${location.latitude} longitude=${location.longitude}")
            Logger.i("accuracy=${location.accuracy} altitude=${location.altitude}")
            Logger.i("bearing=${location.bearing} speed=${location.speed}")
            Logger.i("time=${location.time} elapsedRealtimeNanos=${location.elapsedRealtimeNanos}")
            Logger.i("provider=${location.provider}")
            locationStateFlow.value = location
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
            satelliteStateFlow.value = (0 until status.satelliteCount).map {
                val svid = status.getSvid(it)
                val constellationType = status.getConstellationType(it)
                val cn0DbHz = status.getCn0DbHz(it)
                val elevationDegrees = status.getElevationDegrees(it)
                val azimuthDegrees = status.getAzimuthDegrees(it)
                val hasEphemerisData = status.hasEphemerisData(it)
                val hasAlmanacData = status.hasAlmanacData(it)
                val usedInFix = status.usedInFix(it)
                val hasCarrierFrequencyHz = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    status.hasCarrierFrequencyHz(it)
                } else {
                    false
                }
                val carrierFrequencyHz = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    status.getCarrierFrequencyHz(it)
                } else {
                    Float.NaN
                }
                val hasBasebandCn0DbHz = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    status.hasBasebandCn0DbHz(it)
                } else {
                    false
                }
                val basebandCn0DbHz = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    status.getBasebandCn0DbHz(it)
                } else {
                    Float.NaN
                }
                Satellite(
                    svid, constellationType,
                    cn0DbHz, elevationDegrees,
                    azimuthDegrees, hasEphemerisData,
                    hasAlmanacData, usedInFix,
                    hasCarrierFrequencyHz, carrierFrequencyHz,
                    hasBasebandCn0DbHz, basebandCn0DbHz
                )
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

    private fun legacyNmeaListener() = object : GpsStatus.NmeaListener {
        override fun onNmeaReceived(timestamp: Long, nmea: String?) {
            NmeaStateFlow.value = Nmea(timestamp, nmea ?: "")
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun onNmeaMessageListener() = object : OnNmeaMessageListener {
        override fun onNmeaMessage(message: String?, timestamp: Long) {
            NmeaStateFlow.value = Nmea(timestamp, message ?: "")
        }
    }

}