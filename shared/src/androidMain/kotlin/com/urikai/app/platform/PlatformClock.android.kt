package com.urikai.app.platform

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun todayEpochDay(): Long = java.time.LocalDate.now().toEpochDay()
