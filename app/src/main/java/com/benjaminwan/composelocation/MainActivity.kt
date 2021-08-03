package com.benjaminwan.composelocation

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.afollestad.assent.isAllGranted
import com.benjaminwan.composelocation.app.App
import com.benjaminwan.composelocation.loc.HalopayLocation
import com.benjaminwan.composelocation.loc.HalopayLocationListener
import com.benjaminwan.composelocation.loc.LocationAPI
import com.benjaminwan.composelocation.ui.theme.ComposeLocationTheme
import com.benjaminwan.composelocation.utils.LocManager
import com.benjaminwan.composelocation.utils.makeSureLocEnable
import com.benjaminwan.composelocation.utils.showToast
import com.orhanobut.logger.Logger


class MainActivity : AppCompatActivity() {
    private val latitude = mutableStateOf<Double>(0.0)
    private var longitude = mutableStateOf<Double>(0.0)
    private val locationAPI: LocationAPI = LocationAPI(App.INSTANCE)
    private lateinit var locManager: LocManager

    private fun getPermissions() {
        val permissions = arrayOf(
            Permission.ACCESS_FINE_LOCATION,
            Permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isAllGranted(*permissions)) {
                askForPermissions(*permissions) { result ->
                    val permissionGranted: Boolean = result.isAllGranted(*permissions)
                    if (!permissionGranted) {
                        showToast("权限获取错误！")
                    }
                }
            } else {
                //initLocation()
                locManager = LocManager(applicationContext)
                locManager.startRequestLocationUpdates()
            }
        } else {
            locManager = LocManager(applicationContext)
            locManager.startRequestLocationUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeSureLocEnable(this)
        setContent {
            ComposeLocationTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column {
                        Row {
                            Text(text = "纬度:${latitude.value}", modifier = Modifier.weight(1f))
                            Text(text = "经度:${longitude.value}", modifier = Modifier.weight(1f))
                        }
                        Row {
                            Text(text = "时间", modifier = Modifier.weight(1f))
                            Text(text = "TTFF", modifier = Modifier.weight(1f))
                        }
                        Row {
                            Text(text = "高度", modifier = Modifier.weight(1f))
                            Text(text = "EH ACC", modifier = Modifier.weight(1f))
                        }
                        Row {
                            Text(text = "高度(MSL)", modifier = Modifier.weight(1f))
                            Text(text = "卫星数量", modifier = Modifier.weight(1f))
                        }
                        Row {
                            Text(text = "速度", modifier = Modifier.weight(1f))
                            Text(text = "方位", modifier = Modifier.weight(1f))
                        }
                        Row {
                            Text(text = "速度精度", modifier = Modifier.weight(1f))
                            Text(text = "方位精度", modifier = Modifier.weight(1f))
                        }
                        Row {
                            Text(text = "PDOP", modifier = Modifier.weight(1f))
                            Text(text = "H/V DOP", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        locManager.stop()
        //locationAPI.stopGpsListener()
    }

    private fun initLocation() {
        locationAPI.getChangeCurLocation(object : HalopayLocationListener {
            override fun onLocationChanged(paramYongcheLocation: HalopayLocation?) {
                latitude.value = paramYongcheLocation?.latitude ?: 0.0
                longitude.value = paramYongcheLocation?.longitude ?: 0.0
                Logger.i("纬度：${paramYongcheLocation?.latitude}") //纬度
                Logger.i("经度：${paramYongcheLocation?.longitude}") //经度
            }

            override fun onProviderDisabled(paramString: String?) {
                Logger.i("onProviderDisabled=$paramString")
            }

            override fun onProviderEnabled(paramString: String?) {
                Logger.i("onProviderEnabled=$paramString")
            }

            override fun onStatusChanged(
                paramString: String?,
                paramInt: Int,
                paramBundle: Bundle?
            ) {
                Logger.i("onStatusChanged $paramString $paramInt $paramBundle")
            }

            override fun onLocationFail(paramString: String?) {
                Logger.e("onLocationFail $paramString")
            }

        })
    }
}