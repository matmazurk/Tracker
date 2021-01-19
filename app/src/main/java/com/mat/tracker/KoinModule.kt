package com.mat.tracker

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {

    single { Room.databaseBuilder(
            androidContext(),
            TrackerDatabase::class.java,
            "tracker-db"
        ).build()
    }

    single {
        LocationManager(androidContext())
    }

    single {
        PermissionsDataStore(androidContext())
    }

    factory {
        Repository(get<TrackerDatabase>().locationsDao(), get())
    }
}
