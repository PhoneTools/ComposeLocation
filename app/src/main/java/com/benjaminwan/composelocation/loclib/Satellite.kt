package com.benjaminwan.composelocation.loclib

import android.location.GnssStatus.*
import com.benjaminwan.composelocation.utils.format

data class Satellite(
    val svid: Int,//卫星标识号
    val constellationType: Int = CONSTELLATION_UNKNOWN,//卫星类型
    val cn0DbHz: Float = Float.NaN,//以dB-Hz为单位检索卫星天线处指定索引处的载波与噪声密度
    val elevationDegrees: Float = Float.NaN,//高度角
    val azimuthDegrees: Float = Float.NaN,//方位角
    val hasEphemerisData: Boolean = false,//否具有星历数据
    val hasAlmanacData: Boolean = false,//否具有年历数据
    val usedInFix: Boolean = false,//报告指定指标的卫星是否用于计算最近的位置修正
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

    val cn0DbHzStr: String
        get() = if (cn0DbHz > 0) cn0DbHz.format("0.0") else ""

    val elevationDegreesStr: String
        get() = if (elevationDegrees > 0) elevationDegrees.format("0.0") else ""

    val azimuthDegreesStr: String
        get() = if (azimuthDegrees > 0) azimuthDegrees.format("0.0") else ""
}
