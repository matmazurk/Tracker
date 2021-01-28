package com.mat.tracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationData>)

    @Query("SELECT * FROM LocationData")
    fun getAllLocations() : Flow<List<LocationData>>

    @Query("DELETE FROM LocationData")
    suspend fun nukeTable()

}