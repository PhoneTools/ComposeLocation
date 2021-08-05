package com.benjaminwan.composelocation.loclib

import android.location.Location
import android.os.Build
import com.benjaminwan.composelocation.utils.format
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

val Location.latitudeDegrees: String
    get() = this.latitude.format("0.0000000")

val Location.longitudeDegrees: String
    get() = this.longitude.format("0.0000000")

val Location.latitudeDegreesMinutesSeconds: String
    get() = getDMS(this.latitude).toString()

val Location.longitudeDegreesMinutesSeconds: String
    get() = getDMS(this.longitude).toString()

val Location.latitudeDegreesMinutesSecondsHemisphere: String
    get() {
        val dms = getDMS(this.latitude)
        val hemisphere = if (dms.degrees < 0) "S" else "N"
        return "$hemisphere $dms"
    }

val Location.longitudeDegreesMinutesSecondsHemisphere: String
    get() {
        val dms = getDMS(this.longitude)
        val hemisphere = if (dms.degrees < 0) "W" else "E"
        return "$hemisphere $dms"
    }

val Location.latitudeDegreesDecimalMinutes: String
    get() = getDDM(this.latitude).toString()

val Location.longitudeDegreesDecimalMinutes: String
    get() = getDDM(this.longitude).toString()

val Location.latitudeDegreesDecimalMinutesHemisphere: String
    get() {
        val ddm = getDDM(this.latitude)
        val hemisphere = if (ddm.degrees < 0) "S" else "N"
        return "$hemisphere $ddm"
    }

val Location.longitudeDegreesDecimalMinutesHemisphere: String
    get() {
        val ddm = getDDM(this.longitude)
        val hemisphere = if (ddm.degrees < 0) "W" else "E"
        return "$hemisphere $ddm"
    }

val Location.timeStr: String
    get() = if (this.time > 0L) Date(this.time).format() else ""

val Location.altitudeStr: String
    get() = if (this.hasAltitude()) this.altitude.format("0.0") else ""

val Location.accuracyStr: String
    get() = if (this.hasAccuracy()) this.accuracy.format("0.0") else ""

val Location.verticalAccuracyMeterStr: String
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this.hasVerticalAccuracy()) this.verticalAccuracyMeters.format("0.0") else ""

val Location.speedStr: String
    get() = if (this.hasSpeed()) this.speed.format("0.0") else ""

val Location.speedAccuracyMetersPerSecondStr: String
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this.hasSpeedAccuracy()) this.speedAccuracyMetersPerSecond.format("0.0") else ""

val Location.bearingStr: String
    get() = if (this.hasBearing()) this.bearing.format("0.0") else ""

val Location.bearingAccuracyDegreesStr: String
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this.hasBearingAccuracy()) this.bearingAccuracyDegrees.format("0.0") else ""

private data class DMS(
    val degrees: Int,
    val minutes: Int,
    val seconds: Int,
) {
    override fun toString(): String {
        return "${degrees}\u00B0 ${minutes}\' ${seconds}\""
    }
}

private fun getDMS(coordinate: Double): DMS {
    val loc = BigDecimal(coordinate)
    val degrees = loc.setScale(0, RoundingMode.DOWN)
    val minTemp = loc.subtract(degrees).multiply(BigDecimal(60)).abs()
    val minutes = minTemp.setScale(0, RoundingMode.DOWN)
    val seconds = minTemp.subtract(minutes).multiply(BigDecimal(60)).setScale(2, RoundingMode.HALF_UP)
    return DMS(degrees.toInt(), minutes.toInt(), seconds.toInt())
}

private data class DDM(
    val degrees: Int,
    val minutes: Float,
) {
    override fun toString(): String {
        return "${degrees}\u00B0 ${minutes.format("0.000")}"
    }
}

private fun getDDM(coordinate: Double): DDM {
    val loc = BigDecimal(coordinate)
    val degrees = loc.setScale(0, RoundingMode.DOWN)
    val minutes = loc.subtract(degrees).multiply(BigDecimal(60)).abs().setScale(3, RoundingMode.HALF_UP)
    return DDM(degrees.toInt(), minutes.toFloat())
}