package com.benjaminwan.composelocation

import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.afollestad.assent.isAllGranted
import com.afollestad.assent.runWithPermissions
import com.benjaminwan.composelocation.app.App
import com.benjaminwan.composelocation.ui.theme.ComposeLocationTheme
import com.benjaminwan.composelocation.utils.*


class MainActivity : AppCompatActivity() {
    private val locManager: LocManager = LocManager(App.INSTANCE)
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
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeSureLocEnable(this)
        setContent {
            ComposeLocationTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Column {
                        val location by rememberFlowWithLifecycle(locManager.locationStateFlow).collectAsState(initial = Location(LocationManager.GPS_PROVIDER))
                        val timeToFirstFix by rememberFlowWithLifecycle(locManager.timeToFirstFixStateFlow).collectAsState(initial = Float.NaN)
                        val timeToFirstFixStr = if (timeToFirstFix.isNaN()) "" else timeToFirstFix.format("0.0")
                        val satellites by rememberFlowWithLifecycle(locManager.satelliteStateFlow).collectAsState(initial = emptyList())
                        val usedCount = satellites.count { it.usedInFix }
                        val inViewCount = satellites.count { it.cn0DbHz > 0 }
                        val maxCount = satellites.size
                        val satellitesCountStr = "$usedCount/$inViewCount/$maxCount"
                        LocationInfoCard(location, timeToFirstFixStr, satellitesCountStr)
                        SatelliteListCard(satellites)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getPermissions()
        runWithPermissions(Permission.ACCESS_FINE_LOCATION) {
            locManager.start()
            locManager.addStatusListener()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locManager.stop()
        locManager.removeStatusListener()
    }
}

@Composable
fun LocationInfoCard(location: Location, timeToFirstFix: String, satellitesCountStr: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(4.dp, 2.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 2.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .background(color = MaterialTheme.colors.primary)
                    .fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "位置信息",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colors.onPrimary
                )
            }
            Row {
                LocationText(header = "纬度(度)", content = location.latitudeDegrees, modifier = Modifier.weight(1f))
                LocationText(header = "经度(度)", content = location.longitudeDegrees, modifier = Modifier.weight(1f))
            }
            Row {
                LocationText(header = "纬度(度分秒)", content = location.latitudeDegreesMinutesSeconds, modifier = Modifier.weight(1f))
                LocationText(header = "经度(度分秒)", content = location.longitudeDegreesMinutesSeconds, modifier = Modifier.weight(1f))
            }
            Row {
                LocationText(header = "纬度(度分)", content = location.latitudeDegreesDecimalMinutes, modifier = Modifier.weight(1f))
                LocationText(header = "经度(度分)", content = location.longitudeDegreesDecimalMinutes, modifier = Modifier.weight(1f))
            }
            Row {
                LocationText(header = "时间", content = location.timeStr, modifier = Modifier.weight(1f))
                LocationText(header = "初次定位耗时(秒)", content = timeToFirstFix, modifier = Modifier.weight(1f))
            }
            Row {
                LocationText(header = "海拔(米)", content = location.accuracyStr, modifier = Modifier.weight(1f))
                LocationText(header = "海拔精度(米)", content = location.verticalAccuracyMeterStr, modifier = Modifier.weight(1f))
            }
            Row {
                LocationText(header = "速度(米/秒)", content = location.speedStr, modifier = Modifier.weight(1f))
                LocationText(header = "速度精度(米/秒)", content = location.speedAccuracyMetersPerSecondStr, modifier = Modifier.weight(1f))
            }
            Row {
                LocationText(header = "方位(°)", content = location.bearingStr, modifier = Modifier.weight(1f))
                LocationText(header = "方位精度(°)", content = location.bearingAccuracyDegreesStr, modifier = Modifier.weight(1f))
            }
            Row {
                LocationText(header = "卫星数量(使用/可见/总共)", content = satellitesCountStr, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SatelliteListCard(satellites: List<Satellite>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(4.dp, 2.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 2.dp,
    ) {
        LazyColumn() {
            item {
                Row(modifier = Modifier.background(color = MaterialTheme.colors.primary)) {
                    SatelliteHeaderText(text = "ID", modifier = Modifier.weight(1f))
                    SatelliteHeaderText(text = "TYPE", modifier = Modifier.weight(1f))
                    SatelliteHeaderText(text = "CN0(dB-Hz)", modifier = Modifier.weight(1f))
                    SatelliteHeaderText(text = "高度角(°)", modifier = Modifier.weight(1f))
                    SatelliteHeaderText(text = "方位角(°)", modifier = Modifier.weight(1f))
                }
            }
            items(satellites) { satellite ->
                Row {
                    SatelliteText(text = "${satellite.svid}", modifier = Modifier.weight(1f))
                    SatelliteText(text = satellite.constellation, modifier = Modifier.weight(1f))
                    SatelliteText(text = satellite.cn0DbHzStr, modifier = Modifier.weight(1f))
                    SatelliteText(text = satellite.elevationDegreesStr, modifier = Modifier.weight(1f))
                    SatelliteText(text = satellite.azimuthDegreesStr, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun LocationText(header: String, content: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Start) {
        Text(text = header, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = content, textAlign = TextAlign.Center, fontSize = 12.sp)
    }
}

@Composable
fun SatelliteHeaderText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.caption,
        modifier = modifier,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colors.onPrimary
    )
}

@Composable
fun SatelliteText(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = MaterialTheme.typography.caption, modifier = modifier, textAlign = TextAlign.Center)
}