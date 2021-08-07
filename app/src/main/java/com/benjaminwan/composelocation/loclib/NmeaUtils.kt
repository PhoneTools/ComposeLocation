package com.benjaminwan.composelocation.loclib

//https://gpsd.gitlab.io/gpsd/NMEA.html
/**
 * GNGGA
 * 格式：$GNGGA,<1>,<2>,<3>,<4>,<5>,<6>,<7>,<8>,<9>,<10>,<11>,<12>,<13>,<14>*<15><CR><LF>
<1> UTC时间，格式为hhmmss.sss
<2> 纬度，格式为ddmm.mmmm（前导位数不足则补0）
<3> 纬度半球，N或S（北纬或南纬）
<4> 经度，格式为dddmm.mmmm（前导位数不足则补0）
<5> 经度半球，E或W（东经或西经）
<6> 定位质量指示，0=定位无效，1=定位有效
<7> 使用卫星数量，从00到12（前导位数不足则补0）
<8> 水平精确度，0.5到99.9
<9> 天线离海平面的高度，-9999.9到9999.9米
<10> 高度单位，M表示单位米
<11> 大地椭球面相对海平面的高度（-999.9到9999.9）
<12> 高度单位，M表示单位米
<13> 差分GPS数据期限（RTCM SC-104），最后设立RTCM传送的秒数量
<14> 差分参考基站标号，从0000到1023（前导位数不足则补0）
<15> 校验和
 * */
data class GGA(
    val utc: String,//UTC时间，格式为hhmmss.sss
    val latitude: String, //纬度，格式为ddmm.mmmm（前导位数不足则补0）
    val northSouth: String,//纬度半球，N或S（北纬或南纬）
    val longitude: String,//经度，格式为dddmm.mmmm（前导位数不足则补0）
    val eastWest: String,//经度半球，E或W（东经或西经）
    val quality: String,//定位质量指示
    /** 0 - fix not available,1 - GPS fix,
    2 - Differential GPS fix (values above 2 are 2.3 features)
    3 = PPS fix
    4 = Real Time Kinematic
    5 = Float RTK
    6 = estimated (dead reckoning)
    7 = Manual input mode
    8 = Simulation mode*/
    val satelliteCount: String,//使用卫星数量，从00到12（前导位数不足则补0）
    val hdop: String,//水平精确度，0.5到99.9
    val altitude: String,//天线离海平面的高度，-9999.9到9999.9米
    val altitudeUnit: String,//高度单位，M表示单位米
    val geoidalSeparation: String,//大地椭球面相对海平面的高度（-999.9到9999.9）
    val geoidalSeparationUnit: String,//高度单位，M表示单位米
)

fun nmeaToGGA(nmea: String): GGA? {
    if (nmea.startsWith("\$GPGGA") || nmea.startsWith("\$GNGGA")) {
        val tokens = nmea.split(",")
        val type = if (tokens.size > 0) tokens[0] else ""
        val utc = if (tokens.size > 1) tokens[1] else ""
        val latitude = if (tokens.size > 2) tokens[2] else ""
        val northSouth = if (tokens.size > 3) tokens[3] else ""
        val longitude = if (tokens.size > 4) tokens[4] else ""
        val eastWest = if (tokens.size > 5) tokens[5] else ""
        val quality = if (tokens.size > 6) tokens[6] else ""
        val satellitesCount = if (tokens.size > 7) tokens[7] else ""
        val hdop = if (tokens.size > 8) tokens[8] else ""
        val altitude = if (tokens.size > 9) tokens[9] else ""
        val altitudeUnit = if (tokens.size > 10) tokens[10] else ""
        val geoidalSeparation = if (tokens.size > 11) tokens[11] else ""
        val geoidalSeparationUnit = if (tokens.size > 12) tokens[12] else ""
        return GGA(
            utc, latitude, northSouth, longitude, eastWest,
            quality, satellitesCount, hdop, altitude, altitudeUnit,
            geoidalSeparation, geoidalSeparationUnit,
        )
    }
    return null
}

/**
 * $GNRMC
 * 格式：$GNRMC,<1>,<2>,<3>,<4>,<5>,<6>,<7>,<8>,<9>,<10>,<11>,<12>*<13><CR><LF>
<1> UTC（Coordinated Universal Time）时间，hhmmss（时分秒）格式
<2> 定位状态，A=有效定位，V=无效定位
<3> Latitude，纬度ddmm.mmmm（度分）格式（前导位数不足则补0）
<4> 纬度半球N（北半球）或S（南半球）
<5> Longitude，经度dddmm.mmmm（度分）格式（前导位数不足则补0）
<6> 经度半球E（东经）或W（西经）
<7> 地面速率（000.0~999.9节，Knot，前导位数不足则补0）
<8> 地面航向（000.0~359.9度，以真北为参考基准，前导位数不足则补0）
<9> UTC日期，ddmmyy（日月年）格式
<10> Magnetic Variation，磁偏角（000.0~180.0度，前导位数不足则补0）
<11> Declination，磁偏角方向，E（东）或W（西）
<12> Mode Indicator，模式指示（仅NMEA0183 3.00版本输出，A=自主定位，D=差分，E=估算，N=数据无效）
<13> 校验和
 * */
data class RMC(
    val utc: String,
    val status: String,
    val latitude: String,
    val northSouth: String,
    val longitude: String,
    val eastWest: String,
    val speedOverGround: String,
    val trackMadeGood: String,
    val date: String,
    val magneticVariation: String,
    val declination: String,
    val modeIndicator: String,
)

fun nmeaToRMC(nmea: String): RMC? {
    if (nmea.startsWith("\$GPRMC") || nmea.startsWith("\$GNRMC")) {
        val tokens = nmea.split(",")
        val type = if (tokens.size > 0) tokens[0] else ""
        val utc = if (tokens.size > 1) tokens[1] else ""
        val status = if (tokens.size > 2) tokens[2] else ""
        val latitude = if (tokens.size > 3) tokens[3] else ""
        val northSouth = if (tokens.size > 4) tokens[4] else ""
        val longitude = if (tokens.size > 5) tokens[5] else ""
        val eastWest = if (tokens.size > 6) tokens[6] else ""
        val speedOverGround = if (tokens.size > 7) tokens[7] else ""
        val trackMadeGood = if (tokens.size > 8) tokens[8] else ""
        val date = if (tokens.size > 9) tokens[9] else ""
        val magneticVariation = if (tokens.size > 10) tokens[10] else ""
        val declination = if (tokens.size > 11) tokens[11] else ""
        val modeIndicator = if (tokens.size > 12) tokens[12] else ""
        return RMC(
            utc, status, latitude, northSouth,
            longitude, eastWest, speedOverGround, trackMadeGood,
            date, magneticVariation, declination, modeIndicator,
        )
    }
    return null
}