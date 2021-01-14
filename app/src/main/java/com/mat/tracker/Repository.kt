package com.mat.tracker

import android.location.Location
import kotlinx.coroutines.*

class Repository(
    private val locationsDao: LocationsDao,
    private val locationManager: LocationManager
) {

    suspend fun getAndSaveCurrentLocation() {
        locationManager.getUpdateLocation { onLocationReceived(it) }
    }

    private fun onLocationReceived(location: Location?) = GlobalScope.launch {
        location?.let { it ->
            saveLocation(it.toLocationData())
        }
    }

    suspend fun saveLocation(location: LocationData) =
        locationsDao.insertLocation(location)

    suspend fun clearDatabase() =
        locationsDao.nukeTable()

}