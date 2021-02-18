package com.mat.tracker

import androidx.annotation.MainThread
import org.koin.core.component.KoinComponent

class LocationRepository(
    private val locationsDao: LocationsDao,
    private val locationManager: LocationManager,
) : KoinComponent {

    val receivingLocationUpdates = locationManager.receivingLocationUpdates

    suspend fun getLocations() = locationsDao.getAllLocations()

    suspend fun isAnyLocationRecorded() = locationsDao.isNotEmpty()

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