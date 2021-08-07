package com.benjaminwan.composelocation

import android.location.Location
import android.location.LocationManager.GPS_PROVIDER
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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.afollestad.assent.isAllGranted
import com.afollestad.assent.runWithPermissions
import com.benjaminwan.composelocation.app.App
import com.benjaminwan.composelocation.loclib.*
import com.benjaminwan.composelocation.ui.theme.ComposeLocationTheme
import com.benjaminwan.composelocation.utils.format
import com.benjaminwan.composelocation.utils.rememberFlowWithLifecycle
import com.benjaminwan.composelocation.utils.showToast

class MainActivity : AppCompatActivity() {
    private val locationHelper: LocationHelper = LocationHelper(App.INSTANCE)
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
                        showToast(R.string.request_permission_error)
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
                        val location by rememberFlowWithLifecycle(locationHelper.locationStateFlow).collectAsState(initial = Location(GPS_PROVIDER))
                        val timeToFirstFix by rememberFlowWithLifecycle(locationHelper.timeToFirstFixStateFlow).collectAsState(initial = Float.NaN)
                        val timeToFirstFixStr = if (timeToFirstFix.isNaN()) "" else timeToFirstFix.format("0.0")
                        val satellites by rememberFlowWithLifecycle(locationHelper.satelliteStateFlow).collectAsState(initial = emptyList())
                        val usedCount = satellites.count { it.usedInFix }
                        val inViewCount = satellites.count { it.cn0DbHz > 0 }
                        val maxCount = satellites.size
                        val satellitesCountStr = "$usedCount/$inViewCount/$maxCount"
                        val providerEnable by rememberFlowWithLifecycle(locationHelper.providerStateFlow).collectAsState(initial = false)
                        val nmea by rememberFlowWithLifecycle(locationHelper.nmeaStateFlow).collectAsState(initial = Nmea())
                        var ggaState by remember { mutableStateOf<GGA?>(null) }
                        val gga = nmeaToGGA(nmea.nmea)
                        if (gga != null) ggaState = gga
                        var rmcState by remember { mutableStateOf<RMC?>(null) }
                        val rmc = nmeaToRMC(nmea.nmea)
                        if (rmc != null) rmcState = rmc
                        if (providerEnable) {
                            LocationInfoCard(location, timeToFirstFixStr, satellitesCountStr)
                            if (ggaState != null) GGAInfoCard(ggaState!!)
                            if (rmcState != null) RMCInfoCard(rmcState!!)
                            SatelliteListCard(satellites)
                        } else {
                            Text(
                                text = stringResource(id = R.string.location_service_off_msg),
                                color = MaterialTheme.colors.error, style = MaterialTheme.typography.h6
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getPermissions()
        runWithPermissions(Permission.ACCESS_FINE_LOCATION) {
            locationHelper.start()
            locationHelper.addStatusListener()
            locationHelper.addNmeaListener()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationHelper.stop()
        locationHelper.removeStatusListener()
        locationHelper.removeNmeaListener()
    }
}

@Composable
fun LocationInfoCard(location: Location, timeToFirstFix: String, satellitesCountStr: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(6.dp, 2.dp),
        shape = RoundedCornerShape(6.dp),
        elevation = 2.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .background(color = MaterialTheme.colors.primary)
                    .fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.location_information),
                    textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
                    fontSize = 12.sp, color = MaterialTheme.colors.onPrimary
                )
            }
            Row {
                LocationText(
                    header = stringResource(R.string.location_latitude_degrees),
                    content = location.latitudeDegrees, modifier = Modifier.weight(1f)
                )
                LocationText(
                    header = stringResource(R.string.location_longitude_degrees),
                    content = location.longitudeDegrees, modifier = Modifier.weight(1f)
                )
            }
            Row {
                LocationText(
                    header = stringResource(R.string.location_latitude_degrees_minutes_seconds),
                    content = location.latitudeDegreesMinutesSeconds, modifier = Modifier.weight(1f)
                )
                LocationText(
                    header = stringResource(R.string.location_longitude_degrees_minutes_seconds),
                    content = location.longitudeDegreesMinutesSeconds, modifier = Modifier.weight(1f)
                )
            }
            Row {
                LocationText(
                    header = stringResource(R.string.location_latitude_degrees_decimal_minutes),
                    content = location.latitudeDegreesDecimalMinutes, modifier = Modifier.weight(1f)
                )
                LocationText(
                    header = stringResource(R.string.location_longitude_degrees_decimal_minutes),
                    content = location.longitudeDegreesDecimalMinutes, modifier = Modifier.weight(1f)
                )
            }
            Row {
                LocationText(
                    header = stringResource(R.string.location_time),
                    content = location.timeStr, modifier = Modifier.weight(1f)
                )
                LocationText(
                    header = stringResource(R.string.location_time_to_first_fix),
                    content = timeToFirstFix, modifier = Modifier.weight(1f)
                )
            }
            Row {
                LocationText(
                    header = stringResource(R.string.location_accuracy),
                    content = location.accuracyStr, modifier = Modifier.weight(1f)
                )
            }
            Row {
                LocationText(
                    header = stringResource(R.string.location_altitude),
                    content = location.altitudeStr, modifier = Modifier.weight(1f)
                )
                LocationText(
                    header = stringResource(R.string.location_vertical_accuracy),
                    content = location.verticalAccuracyMeterStr, modifier = Modifier.weight(1f)
                )
            }
            Row {
                LocationText(
                    header = stringResource(R.string.location_speed),
                    content = location.speedStr, modifier = Modifier.weight(1f)
                )
                LocationText(
                    header = stringResource(R.string.location_speed_accuracy),
                    content = location.speedAccuracyMetersPerSecondStr, modifier = Modifier.weight(1f)
                )
            }
            Row {
                LocationText(header = stringResource(R.string.location_bearing), content = location.bearingStr, modifier = Modifier.weight(1f))
                LocationText(
                    header = stringResource(R.string.location_bearing_accuracy),
                    content = location.bearingAccuracyDegreesStr, modifier = Modifier.weight(1f)
                )
            }
            Row {
                LocationText(
                    header = stringResource(R.string.location_satellites_count),
                    content = satellitesCountStr, modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun GGAInfoCard(gga: GGA) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(6.dp, 2.dp),
        shape = RoundedCornerShape(6.dp),
        elevation = 2.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .background(color = MaterialTheme.colors.primary)
                    .fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "GGA",
                    textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
                    fontSize = 12.sp, color = MaterialTheme.colors.onPrimary
                )
            }
            Row {
                LocationText(
                    header = "latitude",
                    content = gga.latitude, modifier = Modifier.weight(1f)
                )
                LocationText(
                    header = "northSouth",
                    content = gga.northSouth, modifier = Modifier.weight(1f)
                )
            }
            Row {
                LocationText(
                    header = "longitude",
                    content = gga.longitude, modifier = Modifier.weight(1f)
                )
                LocationText(
                    header = "eastWest",
                    content = gga.eastWest, modifier = Modifier.weight(1f)
                )
            }
            Row {
                LocationText(
                    header = "utc",
                    content = gga.utc, modifier = Modifier.weight(1f)
                )
                LocationText(
                    header = "quality",
                    content = gga.quality, modifier = Modifier.weight(1f)
                )
            }
            Row {
                LocationText(
                    header = "satelliteCount",
                    content = gga.satelliteCount, modifier = Modifier.weight(1f)
                )
                LocationText(
                    header = "hdop",
                    content = gga.hdop, modifier = Modifier.weight(1f)
                )
            }
            Row {
                LocationText(
                    header = "altitude",
                    content = gga.altitude, modifier = Modifier.weight(1f)
                )
                LocationText(
                    header = "altitudeUnit",
                    content = gga.altitudeUnit, modifier = Modifier.weight(1f)
                )
            }
            Row {
                LocationText(
                    header = "geoidalSeparation",
                    content = gga.geoidalSeparation, modifier = Modifier.weight(1f)
                )
                LocationText(
                    header = "geoidalSeparationUnit",
                    content = gga.geoidalSeparationUnit, modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun RMCInfoCard(rmc: RMC) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(6.dp, 2.dp),
        shape = RoundedCornerShape(6.dp),
        elevation = 2.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .background(color = MaterialTheme.colors.primary)
                    .fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "RMC",
                    textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
                    fontSize = 12.sp, color = MaterialTheme.colors.onPrimary
                )
            }
            Row {
                LocationText(
                    header = "utc",
                    content = rmc.utc, modifier = Modifier.weight(1f)
                )
                LocationText(
                    header = "status",
                    content = rmc.status, modifier = Modifier.weight(1f)
                )
            }
            Row {
                LocationText(
                    header = "latitude",
                    content = rmc.latitude, modifier = Modifier.weight(1f)
                )
                LocationText(
                    header = "northSouth",
                    content = rmc.northSouth, modifier = Modifier.weight(1f)
                )
            }
            Row {
                LocationText(
                    header = "longitude",
                    content = rmc.longitude, modifier = Modifier.weight(1f)
                )
                LocationText(
                    header = "eastWest",
                    content = rmc.eastWest, modifier = Modifier.weight(1f)
                )
            }
            Row {
                LocationText(
                    header = "speedOverGround",
                    content = rmc.speedOverGround, modifier = Modifier.weight(1f)
                )
                LocationText(
                    header = "trackMadeGood",
                    content = rmc.trackMadeGood, modifier = Modifier.weight(1f)
                )
            }
            Row {
                LocationText(
                    header = "date",
                    content = rmc.date, modifier = Modifier.weight(1f)
                )
                LocationText(
                    header = "magneticVariation",
                    content = rmc.magneticVariation, modifier = Modifier.weight(1f)
                )
            }
            Row {
                LocationText(
                    header = "declination",
                    content = rmc.declination, modifier = Modifier.weight(1f)
                )
                LocationText(
                    header = "modeIndicator",
                    content = rmc.modeIndicator, modifier = Modifier.weight(1f)
                )
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
            .padding(6.dp, 2.dp),
        shape = RoundedCornerShape(6.dp),
        elevation = 2.dp,
    ) {
        Column {
            Row(modifier = Modifier.background(color = MaterialTheme.colors.primary)) {
                SatelliteHeaderText(text = "ID", modifier = Modifier.weight(1f))
                SatelliteHeaderText(text = "TYPE", modifier = Modifier.weight(1f))
                SatelliteHeaderText(text = "CN0(dBHz)", modifier = Modifier.weight(1f))
                SatelliteHeaderText(text = stringResource(R.string.elevation_degrees), modifier = Modifier.weight(1f))
                SatelliteHeaderText(text = stringResource(R.string.azimuth_degrees), modifier = Modifier.weight(1f))
            }
            LazyColumn() {
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