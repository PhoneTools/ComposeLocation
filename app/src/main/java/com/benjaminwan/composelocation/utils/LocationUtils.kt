package com.benjaminwan.composelocation.utils

import android.location.Location
import android.os.Build
import java.util.*

val Location.timeStr: String
    get() = if (this.time > 0L) Date(this.time).format() else ""

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