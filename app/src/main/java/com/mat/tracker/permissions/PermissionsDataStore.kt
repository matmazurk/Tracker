package com.mat.tracker.permissions

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.createDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mat.tracker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class PermissionsDataStore(context: Context) {
    private val DENIALS_COUNTER = intPreferencesKey("denials_counter")
    private val dataStore = context.createDataStore(context.getString(R.string.permissions_db_name))

    private val _denials = MutableLiveData<Int>()
    val denials: LiveData<Int>
        get() = _denials

    fun incrementCounterAndFillLiveData() {
        CoroutineScope(Dispatchers.Main).launch {
            incrementBackgroundLocationPermissionDenials()
            val denials = dataStore.data
                .map { permissions ->
                    permissions[DENIALS_COUNTER] ?: 0
                }
            denials
                .take(1)
                .collect {
                    _denials.value = it
                }
        }
    }

    private suspend fun incrementBackgroundLocationPermissionDenials() {
        dataStore.edit { preferences ->
            val current = preferences[DENIALS_COUNTER] ?: 0
            preferences[DENIALS_COUNTER] = current + 1
        }
    }
}