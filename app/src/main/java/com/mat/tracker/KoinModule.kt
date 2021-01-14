package com.mat.tracker

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

    factory {
        Repository(get<TrackerDatabase>().locationsDao(), get())
    }
}
