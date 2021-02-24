package com.mat.tracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import java.lang.IllegalArgumentException
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.TimeUnit

fun Context.hasPermission(permission: String): Boolean {

    if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION &&
        android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q
    ) {
        return true
    }

    return ActivityCompat.checkSelfPermission(this, permission) ==
        PackageManager.PERMISSION_GRANTED
}

fun Long.toHMMSS(): String {
    if (this < 0) {
        throw IllegalArgumentException()
    }
    return String.format(
        "%d:%02d:%02d",
        TimeUnit.MILLISECONDS.toHours(this),
        TimeUnit.MILLISECONDS.toMinutes(this) % 60,
        TimeUnit.MILLISECONDS.toSeconds(this) % 60
    )
}

fun Long.toGpxTime(): String {
    if (this < 0) {
        throw IllegalArgumentException()
    }
    val date = Date(this)
    val calendar = Calendar.getInstance().apply {
        time = date
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val second = calendar.get(Calendar.SECOND)

    return "$year-$month-${day}T$hour:$minute:${second}Z"
}
