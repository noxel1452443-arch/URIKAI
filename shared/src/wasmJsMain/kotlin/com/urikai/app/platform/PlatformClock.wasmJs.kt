package com.urikai.app.platform

private fun jsNow(): Double = js("Date.now()")

private fun jsTimezoneOffsetMinutes(): Double = js("new Date().getTimezoneOffset()")

actual fun currentTimeMillis(): Long = jsNow().toLong()

actual fun todayEpochDay(): Long {
    val localMs = jsNow() - jsTimezoneOffsetMinutes() * 60_000.0
    return kotlin.math.floor(localMs / 86_400_000.0).toLong()
}
