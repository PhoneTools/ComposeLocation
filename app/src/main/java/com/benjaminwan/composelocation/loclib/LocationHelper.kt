package com.benjaminwan.composelocation.loclib

import android.annotation.SuppressLint
import android.content.Context
import android.location.*
import android.os.*
import androidx.annotation.RequiresApi
import androidx.core.location.GnssStatusCompat.*
import com.orhanobut.logger.Logger
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class LocationHelper(context: Context) {
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

    val firstFixTime: Int
        get() = timeToFirstFix.get()
    private val timeToFirstFix: AtomicInteger = AtomicInteger(0)//初次定位耗时 毫秒

    val gpsProviderEnabled: Boolean
        get() = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    val started: Boolean
        get() = isStart.get()
    private val isStart: AtomicBoolean = AtomicBoolean(false) //是否已经启动location Listener

    private val locHandlerThread = HandlerThread("LocationHandlerThread")

    private val onLocationListeners: MutableList<IOnLocationListener> = mutableListOf()

    fun addOnLocationListener(listener: IOnLocationListener?): LocationHelper {
        if (listener == null) return this
        if (!onLocationListeners.contains(listener)) onLocationListeners.add(listener)
        return this
    }

    fun removeOnLocationListener(listener: IOnLocationListener?): LocationHelper {
        if (listener == null || onLocationListeners.isEmpty()) return this
        val index = onLocationListeners.indexOf(listener)
        if (index >= 0) onLocationListeners.removeAt(index)
        return this
    }

    fun clearOnLocationListener() {
        onLocationListeners.clear()
    }

    init {
        locHandlerThread.start()
    }

    @SuppressLint("MissingPermission")
    @JvmOverloads
    fun start(minTimeMs: Long = MIN_TIME_MS, minDistanceM: Float = MIN_DISTANCE_M) {
        Logger.e("start location")
        locationListener().let {
            locationListener = it
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMs, minDistanceM, it, locHandlerThread.looper)
        }
        isStart.set(true)
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        Logger.e("stop location")
        locationListener?.let {
            locationManager.removeUpdates(it)
        }
        isStart.set(false)
    }

    @SuppressLint("MissingPermission")
    fun enableStatusListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            gnssStatusListener().let {
                gnssStatusListener = it
                locationManager.registerGnssStatusCallback(it, Handler(locHandlerThread.looper))
            }
        } else {
            legacyStatusListener().let {
                legacyStatusListener = it
                locationManager.addGpsStatusListener(it)
            }
        }
    }

    fun disableStatusListener() {
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
    fun enableNmeaListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            onNmeaMessageListener().let {
                onNmeaMessageListener = it
                locationManager.addNmeaListener(it, Handler(Looper.getMainLooper()))
            }
        } else {
            legacyNmeaListener().let {
                legacyNmeaListener = it
                locationManager.addNmeaListener(it)
            }
        }
    }

    fun disableNmeaListener() {
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
            onLocationListeners.forEach { it.onLocationChanged(location) }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            //Logger.i("onStatusChanged $provider $status $extras")
        }

        override fun onProviderEnabled(provider: String) {
            //Logger.i("onProviderEnabled $provider")
            onLocationListeners.forEach { it.onGpsProviderChanged(true) }
        }

        override fun onProviderDisabled(provider: String) {
            //Logger.i("onProviderDisabled $provider")
            onLocationListeners.forEach { it.onGpsProviderChanged(false) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun gnssStatusListener() = object : GnssStatus.Callback() {
        override fun onStarted() {
            //Logger.i("onStarted")
            timeToFirstFix.set(0)
        }

        override fun onStopped() {
            //Logger.i("onStopped")
            timeToFirstFix.set(0)
        }

        override fun onFirstFix(ttffMillis: Int) {
            timeToFirstFix.set(ttffMillis)
            onLocationListeners.forEach { it.onFirstFix(ttffMillis) }
        }

        override fun onSatelliteStatusChanged(status: GnssStatus) {
            val satellites = (0 until status.satelliteCount).map {
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
            Logger.e("abc satellites=$satellites")
            onLocationListeners.onEach { it.onSatellitesChanged(satellites) }
        }
    }

    private var mLegacyStatus: GpsStatus? = null

    @SuppressLint("MissingPermission")
    private fun legacyStatusListener() = object : GpsStatus.Listener {
        override fun onGpsStatusChanged(event: Int) {
            when (event) {
                GpsStatus.GPS_EVENT_STARTED -> {
                    //Logger.i("GPS_EVENT_STARTED")
                    timeToFirstFix.set(0)
                }
                GpsStatus.GPS_EVENT_STOPPED -> {
                    //Logger.i("GPS_EVENT_STOPPED")
                    timeToFirstFix.set(0)
                }
                GpsStatus.GPS_EVENT_FIRST_FIX -> {
                    val status = locationManager.getGpsStatus(null)
                    status?.let {
                        val time = it.timeToFirstFix
                        timeToFirstFix.set(time)
                        onLocationListeners.forEach { it.onFirstFix(time) }
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
                        } ?: emptyList()
                        onLocationListeners.forEach { it.onSatellitesChanged(satellites) }
                    }
                }
            }
        }
    }

    private fun legacyNmeaListener() = object : GpsStatus.NmeaListener {
        override fun onNmeaReceived(timestamp: Long, nmea: String?) {
            val tmpNmea = Nmea(timestamp, nmea ?: "")
            onLocationListeners.forEach { it.onNmeaChanged(tmpNmea) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun onNmeaMessageListener() = object : OnNmeaMessageListener {
        override fun onNmeaMessage(message: String?, timestamp: Long) {
            val tmpNmea = Nmea(timestamp, message ?: "")
            onLocationListeners.forEach { it.onNmeaChanged(tmpNmea) }
        }
    }

    /**
     * https://github.com/barbeau/gpstest/blob/master/GPSTest/src/main/java/com/android/gpstest/util/SatelliteUtils.java
     * 卫星标识号。
     * 这个svid是大多数星座的伪随机数。 这是Glonass的FCN和OSN号码。
     * 区分是通过查看星座字段 getConstellationType(int)预期值在以下范围内：
     * GPS: 1-32
     * SBAS: 120-151, 183-192
     * GLONASS: One of: OSN, or FCN+100
     * 1-24 as the orbital slot number (OSN) (preferred, if known)
     * 93-106 as the frequency channel number (FCN) (-7 to +6) plus 100. i.e. encode FCN of -7 as 93, 0 as 100, and +6 as 106
     * QZSS: 193-200
     * Galileo: 1-36
     * Beidou: 1-37
     * */
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