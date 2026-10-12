package com.urikai.app.platform

/** UNIX エポックからのミリ秒。 */
expect fun currentTimeMillis(): Long

/** 端末ローカル日付のエポック日（1970-01-01 = 0）。 */
expect fun todayEpochDay(): Long
