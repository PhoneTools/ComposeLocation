package com.benjaminwan.composelocation.loclib

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.fragment.app.Fragment
import com.benjaminwan.composelocation.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private const val REQUEST_CODE_LOCATION_SOURCE_SETTINGS = 200//转跳到gps开关页面

/**
 * 获取LocationManager
 * @return LocationManager
 */
fun getLocationManager(context: Context): LocationManager =
    context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

/**
 * 位置服务开关是否启用
 * @return Boolean
 */
fun isLocEnable(context: Context): Boolean {
    val locationManager = getLocationManager(context)
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

fun isLocEnableElseDo(context: Context, func: () -> Unit) {
    if (!isLocEnable(context)) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.title_tip)
            .setMessage(R.string.dialog_gps_setting)
            .setCancelable(true)
            .setPositiveButton(
                R.string.dialog_goto_setting
            ) { dialog, which ->
                dialog.dismiss()
                func()
            }.show()
    }
}

fun makeSureLocEnable(context: Activity) {
    isLocEnableElseDo(context) {
        context.startActivityForResult(
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
            REQUEST_CODE_LOCATION_SOURCE_SETTINGS
        )
    }
}

fun makeSureLocEnable(context: Fragment) {
    isLocEnableElseDo(context.requireContext()) {
        context.startActivityForResult(
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
            REQUEST_CODE_LOCATION_SOURCE_SETTINGS
        )
    }
}