package com.mat.tracker.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mat.tracker.data.LocationsDao

@Database(entities = [LocationData::class], version = 1)
abstract class TrackerDatabase : RoomDatabase() {
    abstract fun locationsDao() : LocationsDao
}