package com.mat.tracker

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LocationData::class], version = 1)
abstract class TrackerDatabase : RoomDatabase() {
    abstract fun locationsDao() : LocationsDao
}