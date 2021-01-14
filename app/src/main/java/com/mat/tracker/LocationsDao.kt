package com.mat.tracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLocation(location: LocationData)

    @Query("SELECT * FROM LocationData")
    fun getAllLocations() : List<LocationData>

    @Query("DELETE FROM LocationData")
    fun nukeTable()

}