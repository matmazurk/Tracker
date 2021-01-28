package com.mat.tracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData

class LocationsViewModel(
    private val repository: Repository
) : ViewModel() {

    val locations: LiveData<List<LocationData>>
        get() = repository.getLocations().asLiveData()

    val receivingLocationUpdates: LiveData<Boolean> = repository.receivingLocationUpdates

    fun startTracking() {
        try {
            repository.startTrackingLocation()
        } catch (permissionRevoked: SecurityException) {

        }
    }

    fun stopTracking() =
        repository.stopTrackingLocation()


}