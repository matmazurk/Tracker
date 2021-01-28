package com.mat.tracker

import android.location.Location
import androidx.annotation.MainThread
import kotlinx.coroutines.*
import java.util.concurrent.ExecutorService

class Repository(
    private val locationsDao: LocationsDao,
    private val locationManager: LocationManager,
) {

    val receivingLocationUpdates = locationManager.receivingLocationUpdates

    fun getLocations() =
        locationsDao.getAllLocations()

    @Throws(SecurityException::class)
    @MainThread
    fun startTrackingLocation() =
        locationManager.startLocationUpdates()

    @MainThread
    fun stopTrackingLocation() =
        locationManager.stopLocationUpdates()

    suspend fun saveLocation(location: LocationData) =
        locationsDao.insertLocation(location)

    suspend fun saveLocations(locations: List<LocationData>) =
        locationsDao.insertLocations(locations)

    suspend fun clearDatabase() =
        locationsDao.nukeTable()

}