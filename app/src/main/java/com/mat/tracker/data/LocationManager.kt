package com.mat.tracker.data

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.mat.tracker.LocationUpdatesBroadcastReceiver
import com.mat.tracker.hasPermission
import java.util.concurrent.TimeUnit

class LocationManager(private val context: Context) {

    val receivingLocationUpdates: LiveData<Boolean> get() = _receivingLocationUpdates

    private val _receivingLocationUpdates: MutableLiveData<Boolean> = MutableLiveData(false)
    private val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest: LocationRequest = LocationRequest().apply {
        interval = TimeUnit.SECONDS.toMillis(INTERVAL_DURATION_SECONDS)
        fastestInterval = TimeUnit.SECONDS.toMillis(FASTEST_INTERVAL_DURATION_SECONDS)
        maxWaitTime = TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME_SECONDS)
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private val locationUpdatePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, LocationUpdatesBroadcastReceiver::class.java)
        intent.action = LocationUpdatesBroadcastReceiver.ACTION_LOCATION_UPDATE
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @Throws(SecurityException::class)
    @MainThread
    fun startLocationUpdates() {

        if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return

        try {
            _receivingLocationUpdates.value = true
            fusedLocationClient.requestLocationUpdates(locationRequest, locationUpdatePendingIntent)
        } catch (permissionRevoked: SecurityException) {
            _receivingLocationUpdates.value = false
            throw permissionRevoked
        }
    }

    @MainThread
    fun stopLocationUpdates() {
        _receivingLocationUpdates.value = false
        fusedLocationClient.removeLocationUpdates(locationUpdatePendingIntent)
    }

    companion object {
        private const val INTERVAL_DURATION_SECONDS = 10L
        private const val FASTEST_INTERVAL_DURATION_SECONDS = INTERVAL_DURATION_SECONDS / 2
        private const val MAX_WAIT_TIME_SECONDS = INTERVAL_DURATION_SECONDS * 2
    }
}