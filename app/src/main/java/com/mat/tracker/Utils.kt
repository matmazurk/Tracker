package com.mat.tracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import java.net.URI

fun Context.hasPermission(permission: String): Boolean {

    if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION &&
            android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
        return true
    }

    return ActivityCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
}

fun URI.extractFileName(): String =
    this.path.split('/').lastOrNull()?.substringBeforeLast('.') ?: ""