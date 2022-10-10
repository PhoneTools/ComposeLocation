package com.benjaminwan.composelocation.loclib

import android.location.Location

interface IOnLocationListener {
    fun onLocationChanged(location: Location)//位置信息
    fun onSatellitesChanged(satellites: List<Satellite>)//卫星列表
    fun onNmeaChanged(nmea: Nmea)//原始GPS信息
    fun onFirstFix(firstFixTimeMs: Int)//初次定位时间 毫秒
    fun onGpsProviderChanged(isEnable: Boolean)//定位开关
}