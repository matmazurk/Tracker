package com.mat.tracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import java.net.URI
import java.util.*
import java.util.concurrent.TimeUnit

fun Context.hasPermission(permission: String): Boolean {

    if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION &&
            android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
        return true
    }

    return ActivityCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
}

fun Long.toHMMSS(): String =
    "${TimeUnit.MILLISECONDS.toHours(this)}:" +
    "${TimeUnit.MILLISECONDS.toMinutes(this) % 60}:" +
    String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(this) % 60)

fun Long.toGpxTime(): String {
    val date = Date(this)
    val calendar = Calendar.getInstance().apply {
        time = date
    }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val second = calendar.get(Calendar.SECOND)

    return "$year-$month-${day}T$hour:$minute:${second}Z"

}