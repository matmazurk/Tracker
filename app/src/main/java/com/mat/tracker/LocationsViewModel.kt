package com.mat.tracker

import androidx.lifecycle.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LocationsViewModel(
    private val repository: Repository
) : ViewModel() {

    val locations: LiveData<List<LocationData>> = repository.getLocations().asLiveData()

    val state: LiveData<TrackerActivity.State> = Transformations.map(repository.receivingLocationUpdates) {
        if (it) {
            TrackerActivity.State.TRACING
        } else {
            TrackerActivity.State.NOT_TRACING
        }
    }

    fun saveLocationsToFile(
        filename: String,
        locations: List<LocationData>
    ) =
        viewModelScope.launch {
            repository.writeLocationsToFile(filename, locations)
        }

    fun clearLocations() =
        viewModelScope.launch {
            repository.clearDatabase()
        }

    fun startTracking() {
        try {
            repository.startTrackingLocation()
        } catch (permissionRevoked: SecurityException) {

        }
    }

    fun stopTracking() {
        repository.stopTrackingLocation()
    }
}