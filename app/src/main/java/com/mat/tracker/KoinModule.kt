package com.mat.tracker

import androidx.room.Room
import com.mat.tracker.data.FileRepository
import com.mat.tracker.data.LocationManager
import com.mat.tracker.data.LocationRepository
import com.mat.tracker.data.TrackerDatabase
import com.mat.tracker.permissions.PermissionsDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
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

    single {
        OptionsDataStore(androidContext())
    }

    single {
        FileRepository()
    }

    single {
        LocationRepository(get<TrackerDatabase>().locationsDao(), get())
    }

    viewModel {
        LocationsViewModel(get(), get(), get())
    }
}
