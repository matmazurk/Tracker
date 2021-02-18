package com.mat.tracker

import android.net.Uri
import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class LocationsViewModel(
    private val fileRepository: FileRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    val passedTimeString: LiveData<String>
        get() = _passedTimeString
    val isAnyFileSelected: LiveData<Boolean>
        get() = _isAnyFileSelected
    val selectedPositions: Set<Uri>
        get() = _selectedPositions
    val fileCreationFailure: LiveData<Event<String>>
        get() = _fileCreationFailure
    val newFileEvent = fileRepository.newFileEvent
    val files = fileRepository.files
    val state: LiveData<TrackerActivity.State> = Transformations.map(locationRepository.receivingLocationUpdates) {
        if (it) {
            TrackerActivity.State.TRACING
        } else {
            TrackerActivity.State.NOT_TRACING
        }
    }

    private lateinit var startingTime: Date
    private val _passedTimeString: MutableLiveData<String> = MutableLiveData(null)
    private val _isAnyFileSelected: MutableLiveData<Boolean> = MutableLiveData(false)
    private val _fileCreationFailure: MutableLiveData<Event<String>> = MutableLiveData()
    private val _selectedPositions: MutableSet<Uri> = mutableSetOf()

    fun addFileSelection(uri: Uri) {
        _selectedPositions.add(uri)
        _isAnyFileSelected.value = true
    }

    fun removeFileSelection(uri: Uri) {
        _selectedPositions.remove(uri)
        if (_selectedPositions.isEmpty()) {
            _isAnyFileSelected.value = false
        }
    }

    suspend fun isAnyLocationRecorded() = locationRepository.isAnyLocationRecorded()

    fun clearSelections() = _selectedPositions.clear()

    fun startFileObserver() = fileRepository.startFileObserver()

    fun stopFileObserver() = fileRepository.stopFileObserver()

    fun saveLocationsToFile(filename: String) = viewModelScope.launch {
        val locations = locationRepository.getLocations().filter { it.accuracy <= ACCURACY_THRESHOLD }
        if (locations.isNotEmpty()) {
            fileRepository.writeLocationsToFile(filename, locations)?.let {
                _fileCreationFailure.value =
                        Event("File name already taken.")
            } ?: locationRepository.clearDatabase()
        } else {
            _fileCreationFailure.value =
                Event("Last record doesn't contain correct data, it will be wiped out.")
            locationRepository.clearDatabase()
        }
    }

    fun removeSelectedFiles() {
        _selectedPositions.forEach {
            fileRepository.removeFile(it)
        }
        _selectedPositions.clear()
        _isAnyFileSelected.value = false
    }

    fun clearLocations() =
        viewModelScope.launch {
            locationRepository.clearDatabase()
        }

    fun startTracking() {
        if (state.value != TrackerActivity.State.TRACING) {
            try {
                locationRepository.startTrackingLocation()
                runTimer()
            } catch (permissionRevoked: SecurityException) {

            }
        }
    }

    fun stopTracking() {
        locationRepository.stopTrackingLocation()
    }

    private fun runTimer() = viewModelScope.launch {
        startingTime = Date()
        while (state.value == TrackerActivity.State.TRACING) {
            val difference = Date().time - startingTime.time
            val formattedDifference = difference.toHMMSS()
            _passedTimeString.value = formattedDifference
            delay(1000)
        }
    }

    companion object {
        private const val ACCURACY_THRESHOLD = 20
    }
}