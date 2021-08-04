@file:JvmName("DateUtilsKt")

package com.benjaminwan.composelocation.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 获得默认的 date pattern
 */
private const val defaultDatePattern = "yyyy-MM-dd HH:mm:ss"

/**
 * 根据用户格式返回当前日期

 * @param format
 * *
 * @return
 */
fun getNow(format: String = defaultDatePattern): String {
    return Date().format(format)
}

/**
 * 把Date以参数[pattern]格式化成字符串
 * @param  pattern
 * @return 返回格式化后的字符串
 */
fun Date.format(
    pattern: String = defaultDatePattern,
    timeZone: TimeZone = TimeZone.getDefault()
): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    formatter.timeZone = timeZone
    return formatter.format(this)
}

/**
 * 把Date以"yyyy-MM-dd HH:mm:ss.SSS"格式化成字符串
 * @return 返回格式化后的字符串
 */
fun dateToyyyyMMddHHmmssSSS(input: Date): String =
    input.format("yyyy-MM-dd HH:mm:ss.SSS")

/**
 * 把Date以"HH:mm:ss.SSS"格式化成字符串
 * @return 返回格式化后的字符串
 */
fun dateToHHmmssSSS(input: Date): String =
    input.format("HH:mm:ss.SSS")

/**
 * 把Date以"yyyy-MM-dd HH:mm:ss"格式化成字符串
 * @return 返回格式化后的字符串
 */
fun dateToyyyyMMddHHmmss(input: Date): String =
    input.format("yyyy-MM-dd HH:mm:ss")

/**
 * 把Date以"yyyy-MM-dd"格式化成字符串
 * @return 返回格式化后的字符串
 */
fun dateToyyyyMMdd(input: Date): String =
    input.format("yyyy-MM-dd")

/**
 * 把Date以"HH:mm:ss"格式化成字符串
 * @return 返回格式化后的字符串
 */
fun dateToHHmmss(input: Date): String =
    input.format("HH:mm:ss")

/**
 * 把String以参数[pattern]格式化成Date，格式化失败时返回默认值[defVal]
 * @param  pattern
 * @param  defVal
 * @return 返回Date
 */
fun parseDateOrDef(
    input: String,
    pattern: String = defaultDatePattern,
    defVal: Date = Date(),
    timeZone: TimeZone = TimeZone.getDefault()
): Date {
    if (input.isEmpty()) return defVal
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    formatter.timeZone = timeZone
    return try {
        formatter.parse(input)
    } catch (e: ParseException) {
        e.printStackTrace()
        defVal
    }
}

/**
 * 把String以参数[pattern]格式化成Date，格式化失败时返回null
 * @param  pattern
 * @return 返回Date
 */
fun parseDate(
    input: String,
    pattern: String = defaultDatePattern,
    timeZone: TimeZone = TimeZone.getDefault()
): Date? {
    if (input.isEmpty()) return null
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    formatter.timeZone = timeZone
    return try {
        formatter.parse(input)
    } catch (e: ParseException) {
        e.printStackTrace()
        null
    }
}

/**
 * 把String以参数[inPattern]格式化输入，并以[outputStream]格式化输出
 * @param  inPattern 输入格式
 * @param  outPattern 输出格式
 * @return 返回String
 */
fun dateFormatConvert(
    input: String,
    inPattern: String = "yyyy-MM-dd HH:mm:ss",
    outPattern: String = "yyyy-MM-dd HH:mm:ss"
): String {
    return parseDateOrDef(input, inPattern).format(outPattern)
}

/**
 * 把String以"yyyy-MM-dd HH:mm:ss"格式化输入，并以"yyyyMM"格式化输出
 * @return 返回String
 */
fun yyyyMMddHHmmssToyyyyMM(input: String): String {
    return dateFormatConvert(input, "yyyy-MM-dd HH:mm:ss", "yyyyMM")
}

/**
 * 把String以"yyyy-MM-dd HH:mm:ss"格式化输入，并以"yyyy-MM-dd"格式化输出
 * @return 返回String
 */
fun yyyyMMddHHmmssToyyyyMMdd(input: String): String {
    return dateFormatConvert(input, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd")
}

/**
 * 把String以"yyyy-MM-dd"格式化输入，并以"yyyy年MM月dd日"格式化输出
 * @return 返回String
 */
fun yyyyMMddToNyr(input: String): String {
    return dateFormatConvert(input, "yyyy-MM-dd", "yyyy年MM月dd日")
}

/**
 * 把String以"yyyy-MM-dd HH:mm:ss"格式化输入，并以"yyyy年MM月dd日 HH时mm分"格式化输出
 * @return 返回String
 */
fun yyyyMMddHHmmssToNyrsf(input: String): String {
    return dateFormatConvert(input, "yyyy-MM-dd HH:mm:ss", "yyyy年MM月dd日 HH时mm分")
}

