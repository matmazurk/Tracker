package com.mat.tracker

import androidx.lifecycle.*

class LocationsViewModel(
    private val repository: Repository
) : ViewModel() {

    val locations: LiveData<List<LocationData>>
        get() = repository.getLocations().asLiveData()

    val state: LiveData<TrackerActivity.State> = Transformations.map(repository.receivingLocationUpdates) {
        if (it) {
            TrackerActivity.State.TRACING
        } else {
            TrackerActivity.State.NOT_TRACING
        }
    }

    fun startTracking() {
        try {
            repository.startTrackingLocation()
        } catch (permissionRevoked: SecurityException) {

        }
    }

    fun stopTracking() =
        repository.stopTrackingLocation()


}