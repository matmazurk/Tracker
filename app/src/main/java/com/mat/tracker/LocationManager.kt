package com.mat.tracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

class LocationManager(private val context: Context) {

    fun getUpdateLocation(block: (Location?) -> Unit) {

        val fineLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)

        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener {
                block.invoke(it)
            }
        } else {
            block.invoke(null)
        }
    }
}