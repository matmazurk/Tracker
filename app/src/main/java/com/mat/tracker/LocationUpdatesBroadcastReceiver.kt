package com.mat.tracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LocationUpdatesBroadcastReceiver(
        private val locationRepository: Repository
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == ACTION_LOCATION_UPDATE) {
            LocationResult.extractResult(intent)?.let { locationsResult ->
                val locations = locationsResult.locations.map { location ->
                    location.toLocationData()
                }
                GlobalScope.launch {
                    locationRepository.saveLocations(locations)
                }
            }
        }
    }

    companion object {
        const val ACTION_LOCATION_UPDATE =
            "com.mat.tracker.action.LOCATION_UPDATE"
    }
}