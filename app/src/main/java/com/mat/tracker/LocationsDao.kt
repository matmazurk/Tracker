package com.mat.tracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationData)

    @Query("SELECT * FROM LocationData")
    suspend fun getAllLocations() : List<LocationData>

    @Query("DELETE FROM LocationData")
    suspend fun nukeTable()

}