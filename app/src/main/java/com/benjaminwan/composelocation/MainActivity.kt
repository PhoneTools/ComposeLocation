package com.benjaminwan.composelocation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.tooling.preview.Preview
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.afollestad.assent.isAllGranted
import com.benjaminwan.composelocation.app.App
import com.benjaminwan.composelocation.loc.HalopayLocation
import com.benjaminwan.composelocation.loc.HalopayLocationListener
import com.benjaminwan.composelocation.loc.LocationAPI
import com.benjaminwan.composelocation.ui.theme.ComposeLocationTheme
import com.benjaminwan.composelocation.utils.showToast
import com.orhanobut.logger.Logger

class MainActivity : AppCompatActivity() {
    private val latitude = mutableStateOf<Double>(0.0)
    private var longitude = mutableStateOf<Double>(0.0)
    private val locationAPI: LocationAPI = LocationAPI(App.INSTANCE)

    private fun getPermissions() {
        val permissions = arrayOf(
            Permission.ACCESS_FINE_LOCATION,
            Permission.ACCESS_COARSE_LOCATION
        )
        if (!isAllGranted(*permissions)) {
            askForPermissions(*permissions) { result ->
                val permissionGranted: Boolean = result.isAllGranted(*permissions)
                if (!permissionGranted) {
                    showToast("权限获取错误！")
                }
            }
        } else {
            initLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getPermissions()
        setContent {
            ComposeLocationTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column() {
                        Text(text = "纬度:${latitude.value}")
                        Text(text = "经度:${longitude.value}")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationAPI.stopGpsListener()
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

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeLocationTheme {
        Greeting("Android")
    }
}