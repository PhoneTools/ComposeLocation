package com.benjaminwan.composelocation.utils

import android.location.GnssStatus.*

data class Satellite(
    val svid: Int,
    val constellationType: Int = CONSTELLATION_UNKNOWN,
    val cn0DbHz: Float = Float.NaN,
    val elevationDegrees: Float = Float.NaN,//高度角
    val azimuthDegrees: Float = Float.NaN,//方位角
    val hasEphemerisData: Boolean = false,//星历数据
    val hasAlmanacData: Boolean = false,//年历资料
    val usedInFix: Boolean = false,//用于固定
    val hasCarrierFrequencyHz: Boolean = false,//VERSION_CODES.O 载波频率
    val carrierFrequencyHz: Float = Float.NaN,//VERSION_CODES.O 载波频率
    val hasBasebandCn0DbHz: Boolean = false,//Build.VERSION_CODES.R 基带Cn0
    val basebandCn0DbHz: Float = Float.NaN,//Build.VERSION_CODES.R 基带Cn0
) {
    val constellation: String
        get() = when (constellationType) {
            CONSTELLATION_UNKNOWN -> "UNKNOWN"
            CONSTELLATION_GPS -> "GPS"
            CONSTELLATION_SBAS -> "SBAS"
            CONSTELLATION_GLONASS -> "GLONASS" //格洛纳斯
            CONSTELLATION_QZSS -> "QZSS"
            CONSTELLATION_BEIDOU -> "BEIDOU" //北斗
            CONSTELLATION_GALILEO -> "GALILEO" //伽利略
            CONSTELLATION_IRNSS -> "IRNSS"
            else -> Integer.toString(constellationType)
        }
}
