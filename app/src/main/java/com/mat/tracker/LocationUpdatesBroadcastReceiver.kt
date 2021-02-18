package com.mat.tracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LocationUpdatesBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val locationFileRepository: LocationRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == ACTION_LOCATION_UPDATE) {
            LocationResult.extractResult(intent)?.let { locationsResult ->
                val locations = locationsResult.locations.map { location ->
                    location.toLocationData()
                }
                Log.i("new location", "$locations")
                GlobalScope.launch {
                    locationFileRepository.saveLocations(locations)
                }
            }
        }
    }

    companion object {
        const val ACTION_LOCATION_UPDATE =
            "com.mat.tracker.action.LOCATION_UPDATE"
    }
}