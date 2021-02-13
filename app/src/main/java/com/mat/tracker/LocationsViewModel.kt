package com.mat.tracker

import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class LocationsViewModel(
    private val repository: Repository
) : ViewModel() {

    private lateinit var startingTime: Date
    private val _passedTimeString: MutableLiveData<String?> = MutableLiveData(null)
    val passedTimeString: LiveData<String?>
        get() = _passedTimeString
    val rvSelectedPositions: MutableList<Int> = mutableListOf()
    val locations: LiveData<List<LocationData>> = repository.getLocations().asLiveData()
    val newFileEvent = repository.newFileEvent
    val files = repository.files
    val state: LiveData<TrackerActivity.State> = Transformations.map(repository.receivingLocationUpdates) {
        if (it) {
            TrackerActivity.State.TRACING
        } else {
            TrackerActivity.State.NOT_TRACING
        }
    }

    fun startFileObserver() = repository.startFileObserver()

    fun stopFileObserver() = repository.stopFileObserver()

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
        if (state.value != TrackerActivity.State.TRACING) {
            try {
                repository.startTrackingLocation()
                runTimer()
            } catch (permissionRevoked: SecurityException) {

            }
        }

    }

    fun stopTracking() {
        repository.stopTrackingLocation()
    }

    private fun runTimer() = viewModelScope.launch {
        startingTime = Date()
        while (state.value == TrackerActivity.State.TRACING) {
            val difference = Date().time - startingTime.time
            val formattedDifference =
                    "${TimeUnit.MILLISECONDS.toHours(difference)}:" +
                            "${TimeUnit.MILLISECONDS.toMinutes(difference) % 60}:" +
                            String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(difference) % 60)
            _passedTimeString.postValue(formattedDifference)
            delay(1000)
        }
        _passedTimeString.value = null
    }
}