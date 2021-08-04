package com.benjaminwan.composelocation.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.*
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.location.GnssStatusCompat.*
import kotlinx.coroutines.flow.MutableStateFlow


class LocManager(context: Context) {
    companion object {
        const val MIN_TIME_MS: Long = 1000L //更新间隔(毫秒):1000
        const val MIN_DISTANCE_M: Float = 0.0f //最小距离(当位置距离变化超过此值时更新):0
        //如果最小距离不为0，则以最小距离为准；最小距离为0，则通过时间来定时更新；两者为0，则随时刷新
    }

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var locationListener: LocationListener? = null

    //<=Android M (6.0.1)
    private var legacyStatusListener: GpsStatus.Listener? = null
    private var legacyNmeaListener: GpsStatus.NmeaListener? = null

    //>=Android N (7.0)
    private var gnssStatusListener: GnssStatus.Callback? = null
    private var onNmeaMessageListener: OnNmeaMessageListener? = null

    val satelliteStateFlow = MutableStateFlow<List<Satellite>>(emptyList())//卫星列表
    val NmeaStateFlow = MutableStateFlow<Nmea>(Nmea())//GPS原始数据
    val locationStateFlow = MutableStateFlow<Location>(Location(LocationManager.GPS_PROVIDER))//位置
    val timeToFirstFixStateFlow = MutableStateFlow(Float.NaN)//初次定位时间 TimeToFirstFix
    val providerStateFlow = MutableStateFlow(false) //位置服务开关状态

    @SuppressLint("MissingPermission")
    @JvmOverloads
    fun start(minTimeMs: Long = MIN_TIME_MS, minDistanceM: Float = MIN_DISTANCE_M) {
        locationListener().let {
            locationListener = it
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMs, minDistanceM, it)
        }
        providerStateFlow.value = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        locationListener?.let {
            locationManager.removeUpdates(it)
        }
    }

    @SuppressLint("MissingPermission")
    fun addStatusListener() {
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

    fun removeStatusListener() {
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
    fun addNmeaListener() {
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

    fun removeNmeaListener() {
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
            locationStateFlow.value = location
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            //Logger.i("onStatusChanged $provider $status $extras")
        }

        override fun onProviderEnabled(provider: String) {
            //Logger.i("onProviderEnabled $provider")
            providerStateFlow.value = true
        }

        override fun onProviderDisabled(provider: String) {
            //Logger.i("onProviderDisabled $provider")
            providerStateFlow.value = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun gnssStatusListener() = object : GnssStatus.Callback() {
        override fun onStarted() {
            //Logger.i("onStarted")
            timeToFirstFixStateFlow.value = Float.NaN
        }

        override fun onStopped() {
            //Logger.i("onStopped")
            timeToFirstFixStateFlow.value = Float.NaN
        }

        override fun onFirstFix(ttffMillis: Int) {
            timeToFirstFixStateFlow.value = ttffMillis.toFloat() / 1000
        }

        override fun onSatelliteStatusChanged(status: GnssStatus) {
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
            when (event) {
                GpsStatus.GPS_EVENT_STARTED -> {
                    //Logger.i("GPS_EVENT_STARTED")
                    timeToFirstFixStateFlow.value = Float.NaN
                }
                GpsStatus.GPS_EVENT_STOPPED -> {
                    //Logger.i("GPS_EVENT_STOPPED")
                    timeToFirstFixStateFlow.value = Float.NaN
                }
                GpsStatus.GPS_EVENT_FIRST_FIX -> {
                    val status = locationManager.getGpsStatus(null)
                    status?.let {
                        timeToFirstFixStateFlow.value = it.timeToFirstFix.toFloat() / 1000
                    }
                }
                GpsStatus.GPS_EVENT_SATELLITE_STATUS -> {
                    mLegacyStatus = locationManager.getGpsStatus(mLegacyStatus)
                    val gpsStatus = mLegacyStatus
                    if (gpsStatus != null) {
                        // 获取卫星颗数
                        //val maxSatellites = gpsStatus.maxSatellites
                        //Logger.i("maxSatellites=$maxSatellites")
                        // 卫星列表
                        val satellites = gpsStatus?.satellites?.map {
                            val svid = it.prn
                            val constellationType = getGnssType(it.prn)
                            val cn0DbHz = it.snr
                            val elevationDegrees = it.elevation
                            val azimuthDegrees = it.azimuth
                            val hasEphemerisData = it.hasEphemeris()
                            val hasAlmanacData = it.hasAlmanac()
                            val usedInFix = it.usedInFix()
                            Satellite(
                                svid, constellationType,
                                cn0DbHz, elevationDegrees,
                                azimuthDegrees, hasEphemerisData,
                                hasAlmanacData, usedInFix,
                            )
                        }
                        satelliteStateFlow.value = satellites ?: emptyList()
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

    private fun getGnssType(prn: Int): Int {
        return if (prn >= 1 && prn <= 32) {
            CONSTELLATION_GPS
        } else if (prn == 33) {
            CONSTELLATION_SBAS
        } else if (prn == 39) {
            CONSTELLATION_SBAS
        } else if (prn >= 40 && prn <= 41) {
            CONSTELLATION_SBAS
        } else if (prn == 46) {
            CONSTELLATION_SBAS
        } else if (prn == 48) {
            CONSTELLATION_SBAS
        } else if (prn == 49) {
            CONSTELLATION_SBAS
        } else if (prn == 51) {
            CONSTELLATION_SBAS
        } else if (prn >= 65 && prn <= 96) {
            CONSTELLATION_GLONASS
        } else if (prn >= 193 && prn <= 200) {
            CONSTELLATION_QZSS
        } else if (prn >= 201 && prn <= 235) {
            CONSTELLATION_BEIDOU
        } else if (prn >= 301 && prn <= 336) {
            CONSTELLATION_GALILEO
        } else {
            CONSTELLATION_UNKNOWN
        }
    }

}