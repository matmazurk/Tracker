package com.mat.tracker

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.createDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take

class OptionsDataStore(context: Context) {
    private val NAME_KEY = stringPreferencesKey("name")
    private val DESCRIPTION_KEY = stringPreferencesKey("description")
    private val ACCURACY_KEY = intPreferencesKey("accuracy")
    private val dataStore = context.createDataStore(context.getString(R.string.options_db_name))

    val authorNameFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[NAME_KEY] ?: ""
        }
        .take(1)
    val recordingDescriptionFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[DESCRIPTION_KEY] ?: ""
        }
        .take(1)
    val accuracyThresholdFlow: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[ACCURACY_KEY] ?: 20
        }
        .take(1)


    suspend fun saveAuthorName(name: String) {
        dataStore.edit { preferences ->
            preferences[NAME_KEY] = name
        }
    }

    suspend fun saveRecordingDescription(description: String) {
        dataStore.edit { preferences ->
            preferences[DESCRIPTION_KEY] = description
        }
    }

    suspend fun saveAccuracyThreshold(threshold: Int) {
        dataStore.edit { preferences ->
            preferences[ACCURACY_KEY] = threshold
        }
    }

}