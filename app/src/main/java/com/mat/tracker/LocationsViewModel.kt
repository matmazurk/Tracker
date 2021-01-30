package com.mat.tracker

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LocationsViewModel(
    private val repository: Repository
) : ViewModel() {

    private val locations: Flow<List<LocationData>>
        get() = repository.getLocations()

    val state: LiveData<TrackerActivity.State> = Transformations.map(repository.receivingLocationUpdates) {
        if (it) {
            TrackerActivity.State.TRACING
        } else {
            TrackerActivity.State.NOT_TRACING
        }
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
        viewModelScope.launch {
            locations.collect {

            }
        }
    }
}