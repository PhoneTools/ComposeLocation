package com.benjaminwan.composelocation

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.afollestad.assent.isAllGranted
import com.afollestad.assent.runWithPermissions
import com.benjaminwan.composelocation.app.App
import com.benjaminwan.composelocation.ui.theme.ComposeLocationTheme
import com.benjaminwan.composelocation.utils.*


class MainActivity : AppCompatActivity() {
    private val latitude = mutableStateOf<Double>(0.0)
    private var longitude = mutableStateOf<Double>(0.0)
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
                        val satellites by rememberFlowWithLifecycle(locManager.satelliteStateFlow).collectAsState(initial = emptyList())
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
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locManager.stop()
        //locationAPI.stopGpsListener()
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
                    SatelliteHeaderText(text = "C/N0", modifier = Modifier.weight(1f))
                    SatelliteHeaderText(text = "高度角", modifier = Modifier.weight(1f))
                    SatelliteHeaderText(text = "方位角", modifier = Modifier.weight(1f))
                }
            }
            items(satellites) { satellite ->
                Row {
                    SatelliteText(text = "${satellite.svid}", modifier = Modifier.weight(1f))
                    SatelliteText(text = satellite.constellation, modifier = Modifier.weight(1f))
                    SatelliteText(text = "${satellite.cn0DbHz}", modifier = Modifier.weight(1f))
                    SatelliteText(text = "${satellite.elevationDegrees}", modifier = Modifier.weight(1f))
                    SatelliteText(text = "${satellite.azimuthDegrees}", modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun SatelliteHeaderText(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = MaterialTheme.typography.caption, modifier = modifier, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,color = MaterialTheme.colors.onPrimary)
}

@Composable
fun SatelliteText(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = MaterialTheme.typography.caption, modifier = modifier, textAlign = TextAlign.Center)
}